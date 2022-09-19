def call() {
  node() {
    common.pipelineInit()
    stage('Download Dependencies') {
      sh '''go mod init dispatch
      go get
      go build'''
    }
  }
}