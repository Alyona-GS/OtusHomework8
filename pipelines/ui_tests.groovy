import groovy.json.JsonSlurperClassic
timeout(360) {
    node('maven') {
        stage('Checkout') {
            checkout scm
        }

        def yamlConfig = readYaml text: ${YAML_CONFIG}

        stage('Running tests') {
            def exitCode = sh(
                    script: "mvn test -Dbrowser=${yamlConfig.BROWSER} -DbrowserVersion=${yamlConfig.BROWSER_VERSION}",
                    returnStatus: true
            )
            if(exitCode > 0) {
                currentBuild.status = 'UNSTABLE'
            }
        }

        stage('Publish allure') {
            script('Publish allure report') {
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
        }

        stage('Send notification') {
            def report = readFile './allure-report/widgets/summary.json'
            def slurped = new JsonSlurperClassic().parseText(report)
        }
    }
}

def getNotifyMessage(statistic) {
    def message = "-------- Report ----------\n"
    statistic.each { k, v ->
        message += "\t${k}: ${v}\n"
    }
    sh ""
}