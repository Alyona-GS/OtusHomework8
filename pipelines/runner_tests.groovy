timeout(300) {
    node('python') {
        currentBuild.description = """
        BRANCH=${REFSPEC}
        Owner=${BUILD_USER}
        """
        stage('Checkout') {
            dir('api-tests') {
                checkout scm
            }
        }

        def jobs = [:]
        def yamlConfig = readYaml text: $YAML_CONFIG

        for(def type in yamlConfig.TESTS_TYPE) {
            jobs.type = node('python') {
                stage("RUNNING ${type} tests") {
                    sh "docker run ..."
                }
            }
        }

        parallel jobs

        stage('Copy allure artifacts') {
            copyArtifacts()
        }
    }
}