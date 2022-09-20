def call() {
  env.EXTRA_OPTS=""
  node() {
    common.pipelineInit()
    stage('Download Dependencies') {
      sh '''go mod init dispatch
      go get
      go build'''
    }
    common.codeChecks()

    if(env.BRANCH_NAME == env.TAG_NAME)
    {
      common.publishArtifact()
    }
  }
}