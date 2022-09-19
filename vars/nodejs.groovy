def call() {
  node() {
    common.pipelineInit()
    stage('Download dependencies') {
      sh '''ls -ltr
      npm install'''
    }
    sh 'env'
  }
}