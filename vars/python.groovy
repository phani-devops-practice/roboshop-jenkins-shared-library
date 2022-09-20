def call() {
  node() {
    common.pipelineInit()
    common.codeChecks()
    if(env.BRANCH_NAME == env.TAG_NAME)
    {
     common.publishArtifact()

    }
  }
}