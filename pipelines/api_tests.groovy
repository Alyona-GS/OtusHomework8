import groovy.json.JsonSlurperClassic
timeout(360) {
    node('maven') {
        stage('Checkout') {
            checkout scm
            dir('api_tests') {
                git "https://github.com/Alyona-GS/OtusHomework3.git"
            }
        }

        stage('Running tests') {
            def exitCode = sh(
                    script: "cd api_tests; mvn -B -e -X test",
                    returnStatus: true
            )
            if(exitCode > 0) {
                currentBuild.status = 'UNSTABLE'
            }
        }

        stage('Publish allure') {
            dir('api-tests') {
                allure([
                        includeProperties: false,
                        jdk              : '',
                        properties       : [],
                        reportBuildPolicy: 'ALWAYS',
                        results          : [[path: './allure-results']]
                ])
            }
        }

        stage('Send notification') {
            def report = readFile './api-tests/allure-report/widgets/summary.json'
            def slurped = new JsonSlurperClassic().parseText(report)
            getNotifyMessage(slurped)
        }
    }
}

def getNotifyMessage(statistic) {
    def message = "-------- Report ----------\n"
    statistic.each { k, v ->
        message += "\t${k}: ${v}\n"
    }
    sh(echo message)

    withCredentials(string([credentialsId: chat_id, var: chat_id]), string([credentialsId: token, var: botToken])) {
        sh "curl -s -X POST -H 'Content-Type: application/json' -d '{\"chat_id\": \"${chat_id}\", \"text\": \"${message}\"}' https://api.telegram.org/bot${botToken}/sendMessage\n"
    }
}