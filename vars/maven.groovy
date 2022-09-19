def call() {
    node() {
        common.pipelineInit()
        stage('Compile packages') {
          sh 'mvn clean package'
        }
    }
}