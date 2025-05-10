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
                    sh "docker run ... localhost:5005/${type}_tests:1.0.0"
                }
            }
        }

        parallel jobs

        stage('Copy allure artifacts') {
//            jobs.each { k, job -> {
//                dir('allure-results') {
//                    copyArtifacts filter: 'allure-reports.zip', fingerprintArtifacts: true, projectName: job.Job_Name, selector: specific(job.BUILD_NUMBER)
//                }
//            })
        }
    }
}