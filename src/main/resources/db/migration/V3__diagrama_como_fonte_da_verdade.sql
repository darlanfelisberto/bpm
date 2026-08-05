-- Pivot: a edicao passa a acontecer direto no diagrama (bpmn-js Modeler),
-- nao mais via formularios de atividade/raia/fluxo. O XML do diagrama
-- passa a ser a fonte da verdade, guardado em processo.xml_bpmn.

ALTER TABLE processo ADD COLUMN xml_bpmn TEXT;

-- Backfill do processo de exemplo (Solicitação de Férias) com o XML
-- equivalente ao que era gerado dinamicamente pelas tabelas abaixo,
-- antes de serem removidas, para nao perder o exemplo ja cadastrado.
UPDATE processo SET xml_bpmn = '<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="Definitions_1" targetNamespace="https://iffar.edu.br/bpm">
  <bpmn:process id="Process_1" name="Solicitação de Férias" isExecutable="false">
    <bpmn:laneSet id="LaneSet_1">
      <bpmn:lane id="Lane_1" name="Servidor">
        <bpmn:flowNodeRef>Elemento_1</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Elemento_2</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_2" name="Chefia Imediata">
        <bpmn:flowNodeRef>Elemento_3</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Elemento_5</bpmn:flowNodeRef>
      </bpmn:lane>
      <bpmn:lane id="Lane_3" name="RH">
        <bpmn:flowNodeRef>Elemento_4</bpmn:flowNodeRef>
        <bpmn:flowNodeRef>Elemento_6</bpmn:flowNodeRef>
      </bpmn:lane>
    </bpmn:laneSet>
    <bpmn:startEvent id="Elemento_1" name="Início">
      <bpmn:outgoing>Fluxo_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:task id="Elemento_2" name="Preencher formulário de férias">
      <bpmn:incoming>Fluxo_1</bpmn:incoming>
      <bpmn:outgoing>Fluxo_2</bpmn:outgoing>
    </bpmn:task>
    <bpmn:exclusiveGateway id="Elemento_3" name="Aprovação da chefia?">
      <bpmn:incoming>Fluxo_2</bpmn:incoming>
      <bpmn:outgoing>Fluxo_3</bpmn:outgoing>
      <bpmn:outgoing>Fluxo_4</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:task id="Elemento_4" name="Registrar férias no sistema">
      <bpmn:incoming>Fluxo_3</bpmn:incoming>
      <bpmn:outgoing>Fluxo_5</bpmn:outgoing>
    </bpmn:task>
    <bpmn:task id="Elemento_5" name="Notificar indeferimento">
      <bpmn:incoming>Fluxo_4</bpmn:incoming>
      <bpmn:outgoing>Fluxo_6</bpmn:outgoing>
    </bpmn:task>
    <bpmn:endEvent id="Elemento_6" name="Fim">
      <bpmn:incoming>Fluxo_5</bpmn:incoming>
      <bpmn:incoming>Fluxo_6</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Fluxo_1" sourceRef="Elemento_1" targetRef="Elemento_2"/>
    <bpmn:sequenceFlow id="Fluxo_2" sourceRef="Elemento_2" targetRef="Elemento_3"/>
    <bpmn:sequenceFlow id="Fluxo_3" sourceRef="Elemento_3" targetRef="Elemento_4" name="Aprovado"/>
    <bpmn:sequenceFlow id="Fluxo_4" sourceRef="Elemento_3" targetRef="Elemento_5" name="Indeferido"/>
    <bpmn:sequenceFlow id="Fluxo_5" sourceRef="Elemento_4" targetRef="Elemento_6"/>
    <bpmn:sequenceFlow id="Fluxo_6" sourceRef="Elemento_5" targetRef="Elemento_6"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="Diagram_1">
    <bpmndi:BPMNPlane id="Plane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="Lane_1_di" bpmnElement="Lane_1" isHorizontal="true">
        <dc:Bounds x="160" y="60" width="1440" height="150"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_2_di" bpmnElement="Lane_2" isHorizontal="true">
        <dc:Bounds x="160" y="210" width="1440" height="150"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Lane_3_di" bpmnElement="Lane_3" isHorizontal="true">
        <dc:Bounds x="160" y="360" width="1440" height="150"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Elemento_1_di" bpmnElement="Elemento_1">
        <dc:Bounds x="260" y="117" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Elemento_2_di" bpmnElement="Elemento_2">
        <dc:Bounds x="440" y="95" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Elemento_3_di" bpmnElement="Elemento_3">
        <dc:Bounds x="620" y="260" width="50" height="50"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Elemento_4_di" bpmnElement="Elemento_4">
        <dc:Bounds x="800" y="395" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Elemento_5_di" bpmnElement="Elemento_5">
        <dc:Bounds x="980" y="245" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Elemento_6_di" bpmnElement="Elemento_6">
        <dc:Bounds x="1160" y="417" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Fluxo_1_di" bpmnElement="Fluxo_1">
        <di:waypoint x="296" y="135"/>
        <di:waypoint x="440" y="135"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Fluxo_2_di" bpmnElement="Fluxo_2">
        <di:waypoint x="540" y="135"/>
        <di:waypoint x="620" y="285"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Fluxo_3_di" bpmnElement="Fluxo_3">
        <di:waypoint x="670" y="285"/>
        <di:waypoint x="800" y="435"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Fluxo_4_di" bpmnElement="Fluxo_4">
        <di:waypoint x="670" y="285"/>
        <di:waypoint x="980" y="285"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Fluxo_5_di" bpmnElement="Fluxo_5">
        <di:waypoint x="900" y="435"/>
        <di:waypoint x="1160" y="435"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Fluxo_6_di" bpmnElement="Fluxo_6">
        <di:waypoint x="1080" y="285"/>
        <di:waypoint x="1160" y="435"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>'
WHERE nome = 'Solicitação de Férias';

-- Tabelas do CRUD granular (raia/atividade/fluxo), substituido pela edicao
-- direta no diagrama. Drop na ordem que respeita as FKs.
DROP TABLE fluxo_sequencia;
DROP TABLE atividade;
DROP TABLE raia;
DROP TABLE tipo_elemento;
DROP TABLE diagrama_gerado;
