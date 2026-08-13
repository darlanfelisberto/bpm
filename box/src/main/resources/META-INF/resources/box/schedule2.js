(function () {
    'use strict';

    // Igual ao schedule.js (b:schedule), so que sem FullCalendar: a grade
    // do mes, a navegacao entre meses e o drag-and-drop sao tudo escrito
    // na mao aqui. So visao de mes (sem semana/dia, sem redimensionar -
    // exigiria uma grade de horas, a parte mais trabalhosa de reproduzir).
    // Multi-dia: o mesmo evento aparece repetido em cada dia que ele toca
    // (sem a barra continua que o FullCalendar desenha).

    function lerEventos(wrapper) {
        var script = wrapper.querySelector('.box-schedule2-eventos');
        return script ? JSON.parse(script.textContent) : [];
    }

    function doisDigitos(n) {
        return n < 10 ? '0' + n : '' + n;
    }

    function formatarData(data) {
        return data.getFullYear() + '-' + doisDigitos(data.getMonth() + 1) + '-' + doisDigitos(data.getDate());
    }

    function formatarDataHora(data) {
        return formatarData(data) + 'T' + doisDigitos(data.getHours()) + ':'
                + doisDigitos(data.getMinutes()) + ':' + doisDigitos(data.getSeconds());
    }

    function somenteData(data) {
        return new Date(data.getFullYear(), data.getMonth(), data.getDate());
    }

    function somarDias(data, dias) {
        var copia = new Date(data);
        copia.setDate(copia.getDate() + dias);
        return copia;
    }

    // Semanas completas (dom-sab) que cobrem o mes inteiro - inclui dias
    // do mes anterior/seguinte pra preencher a primeira/ultima semana.
    function gerarSemanas(ano, mes) {
        var primeiroDoMes = new Date(ano, mes, 1);
        var cursor = somarDias(primeiroDoMes, -primeiroDoMes.getDay());
        var ultimoDoMes = new Date(ano, mes + 1, 0);
        var semanas = [];
        do {
            var semana = [];
            for (var i = 0; i < 7; i++) {
                semana.push(new Date(cursor));
                cursor = somarDias(cursor, 1);
            }
            semanas.push(semana);
        } while (cursor <= ultimoDoMes);
        return semanas;
    }

    // Um Schedule2Grade por wrapper .box-schedule2 - guarda o mes/ano em
    // exibicao e os eventos, e sabe (re)desenhar a grade a partir desse
    // estado.
    class Schedule2Grade {
        constructor(wrapper) {
            this.wrapper = wrapper;
            this.eventos = lerEventos(wrapper);
            var hoje = new Date();
            this.ano = hoje.getFullYear();
            this.mes = hoje.getMonth();
        }

        eventosNoDia(dia) {
            return this.eventos.filter(function (evento) {
                if (!evento.start) {
                    return false;
                }
                var inicio = somenteData(new Date(evento.start));
                var fim = evento.end ? somenteData(new Date(evento.end)) : inicio;
                return dia >= inicio && dia <= fim;
            });
        }

        moverEvento(eventoId, novoDia) {
            var evento = this.eventos.find(function (e) { return e.id === eventoId; });
            if (!evento) {
                return;
            }
            var inicioOriginal = new Date(evento.start);
            var diasDeDiferenca = Math.round((novoDia - somenteData(inicioOriginal)) / 86400000);
            if (diasDeDiferenca === 0) {
                return;
            }
            var novoInicio = somarDias(inicioOriginal, diasDeDiferenca);
            evento.start = formatarDataHora(novoInicio);
            var novoFimTexto = evento.start;
            if (evento.end) {
                var novoFim = somarDias(new Date(evento.end), diasDeDiferenca);
                evento.end = formatarDataHora(novoFim);
                novoFimTexto = evento.end;
            }

            this.renderizarGrade();

            var dados = {};
            dados[this.wrapper.id + '_eventoId'] = eventoId;
            dados[this.wrapper.id + '_inicio'] = evento.start;
            dados[this.wrapper.id + '_fim'] = novoFimTexto;
            window.Box.disparar(this.wrapper, 'move', dados);
        }

        criarCelula(dia) {
            var self = this;
            var celula = document.createElement('div');
            celula.className = 'box-schedule2-dia';
            if (dia.getMonth() !== this.mes) {
                celula.classList.add('box-schedule2-dia-fora');
            }
            var hoje = somenteData(new Date());
            if (dia.getTime() === hoje.getTime()) {
                celula.classList.add('box-schedule2-dia-hoje');
            }

            var numero = document.createElement('span');
            numero.className = 'box-schedule2-dia-numero';
            numero.textContent = dia.getDate();
            celula.appendChild(numero);

            this.eventosNoDia(dia).forEach(function (evento) {
                var chip = document.createElement('div');
                chip.className = 'box-schedule2-evento';
                chip.textContent = evento.title;
                chip.draggable = true;
                if (evento.color) {
                    chip.style.backgroundColor = evento.color;
                }
                chip.addEventListener('dragstart', function (dragEvent) {
                    dragEvent.dataTransfer.setData('text/plain', evento.id);
                    dragEvent.dataTransfer.effectAllowed = 'move';
                });
                chip.addEventListener('click', function (clickEvent) {
                    clickEvent.stopPropagation();
                    var dados = {};
                    dados[self.wrapper.id + '_eventoId'] = evento.id;
                    window.Box.disparar(self.wrapper, 'click', dados);
                });
                celula.appendChild(chip);
            });

            celula.addEventListener('dragover', function (dragEvent) {
                dragEvent.preventDefault();
                dragEvent.dataTransfer.dropEffect = 'move';
            });
            celula.addEventListener('drop', function (dropEvent) {
                dropEvent.preventDefault();
                var eventoId = dropEvent.dataTransfer.getData('text/plain');
                self.moverEvento(eventoId, dia);
            });
            celula.addEventListener('click', function () {
                var dados = {};
                dados[self.wrapper.id + '_inicio'] = formatarData(dia);
                dados[self.wrapper.id + '_fim'] = formatarData(somarDias(dia, 1));
                window.Box.disparar(self.wrapper, 'select', dados);
            });

            return celula;
        }

        renderizarGrade() {
            var self = this;
            var alvo = this.wrapper.querySelector('.box-schedule2-calendario');
            alvo.replaceChildren();

            var cabecalho = document.createElement('div');
            cabecalho.className = 'box-schedule2-cabecalho';

            var botaoAnterior = document.createElement('button');
            botaoAnterior.type = 'button';
            botaoAnterior.textContent = '‹';
            botaoAnterior.addEventListener('click', function () {
                self.mes -= 1;
                if (self.mes < 0) {
                    self.mes = 11;
                    self.ano -= 1;
                }
                self.renderizarGrade();
            });

            var botaoProximo = document.createElement('button');
            botaoProximo.type = 'button';
            botaoProximo.textContent = '›';
            botaoProximo.addEventListener('click', function () {
                self.mes += 1;
                if (self.mes > 11) {
                    self.mes = 0;
                    self.ano += 1;
                }
                self.renderizarGrade();
            });

            var titulo = document.createElement('span');
            titulo.className = 'box-schedule2-titulo';
            var textoTitulo = new Date(this.ano, this.mes, 1)
                    .toLocaleDateString('pt-BR', {month: 'long', year: 'numeric'});
            // so a primeira letra maiuscula ("Agosto de 2026") - text-transform:
            // capitalize maiusculizaria cada palavra ("Agosto De 2026").
            titulo.textContent = textoTitulo.charAt(0).toUpperCase() + textoTitulo.slice(1);

            cabecalho.appendChild(botaoAnterior);
            cabecalho.appendChild(titulo);
            cabecalho.appendChild(botaoProximo);
            alvo.appendChild(cabecalho);

            var grade = document.createElement('div');
            grade.className = 'box-schedule2-grade';

            var formatadorDiaSemana = new Intl.DateTimeFormat('pt-BR', {weekday: 'short'});
            for (var i = 0; i < 7; i++) {
                var cabecalhoDia = document.createElement('div');
                cabecalhoDia.className = 'box-schedule2-cabecalho-dia';
                cabecalhoDia.textContent = formatadorDiaSemana.format(new Date(2026, 0, 4 + i));
                grade.appendChild(cabecalhoDia);
            }

            gerarSemanas(this.ano, this.mes).forEach(function (semana) {
                semana.forEach(function (dia) {
                    grade.appendChild(self.criarCelula(dia));
                });
            });

            alvo.appendChild(grade);
        }
    }

    function iniciarSchedule2(wrapper) {
        if (wrapper.dataset.boxSchedule2Iniciado === 'true') {
            return;
        }
        wrapper.dataset.boxSchedule2Iniciado = 'true';

        new Schedule2Grade(wrapper).renderizarGrade();
    }

    function iniciarTodos(raiz) {
        (raiz || document).querySelectorAll('.box-schedule2').forEach(iniciarSchedule2);
    }

    window.Box.aoProntoOuAjax(iniciarTodos);
})();
