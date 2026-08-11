import { defineConfig } from 'vite';

// Empacotado no WAR sob o contexto /responder (ver pom.xml); o backend
// Java fica em /api. Build gerado fora de frontend/ para não versionar
// artefato de build (mesma convenção do target/ do Maven).
export default defineConfig({
  base: '/responder/',
  build: {
    outDir: '../target/responder-dist',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      '/api': 'http://localhost:9080',
    },
  },
});
