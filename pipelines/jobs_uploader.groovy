timeout(300) {
    node('maven') {
        currentBuild.description = """
        BRANCH=${REFSPEC}
        Owner=${env.BUILD_USER}
        """
        stage('Checkout') {
            dir('api-tests') {
                checkout scm
            }
        }
        def parameters = load './pipelines/parameters.groovy'

        stage('Generate job.ini config') {
            dir('api-tests') {
                withCredentials([usernamePassword(credentialsId: 'jobs_builder_creds', usernameVariable: 'username', passwordVariable: 'password')]) {
                    sh "USER=${username} PASSWORD=${password} python3 ${parameters.configScriptPath}"
                }
            }

            stage('Start update jobs') {
                dir('api-tests') {
                    sh "/home/asgor/.local/bin/jenkins-jobs --conf /home/asgor/jenkins/config/job.ini update /home/asgor/jenkins/jobs/"
                }
            }
        }
    }
}