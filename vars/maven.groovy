def call() {
  node() {
    common.pipelineInit()
    stage('Compile packages') {
      sh 'mvn clean package'
    }
    common.codeChecks()

    if(env.BRANCH_NAME == env.TAG_NAME)
    {
      common.publishArtifact()
    }
  }
}