// Notification helper for Jenkins pipelines
// Provides email notifications for build start, success, and failure

def sendStartNotification() {
    echo "Sending build start notification..."

    def subject = "Build Started: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    def body = """
    <h2>Build Started</h2>
    <ul>
        <li><b>Job:</b> ${env.JOB_NAME}</li>
        <li><b>Build Number:</b> ${env.BUILD_NUMBER}</li>
        <li><b>Branch:</b> ${env.BRANCH_NAME}</li>
        <li><b>Started by:</b> ${currentBuild.getBuildCauses()[0]?.userName ?: 'Timer/SCM'}</li>
        <li><b>Build URL:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
    </ul>
    """

    // Send email if configured
    try {
        emailext(
            subject: subject,
            body: body,
            mimeType: 'text/html',
            to: "${env.NOTIFICATION_EMAIL ?: 'dev-team@example.com'}"
        )
    } catch (err) {
        echo "Failed to send email notification: ${err}"
    }
}

def sendEmailNotification() {
    echo "Sending build completion notification..."

    def buildStatus = currentBuild.result ?: 'SUCCESS'
    def statusColor = buildStatus == 'SUCCESS' ? '#00FF00' : '#FF0000'
    def statusIcon = buildStatus == 'SUCCESS' ? '✅' : '❌'

    def subject = "${statusIcon} Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"

    // Read email template
    def templatePath = "${env.WORKSPACE}/../pipeline-build/resources/email-template.html"
    def emailBody = ""

    try {
        if (fileExists(templatePath)) {
            emailBody = readFile(templatePath)

            // Replace template variables
            emailBody = emailBody
                .replace('{{JOB_NAME}}', env.JOB_NAME)
                .replace('{{BUILD_NUMBER}}', env.BUILD_NUMBER.toString())
                .replace('{{BUILD_STATUS}}', buildStatus)
                .replace('{{STATUS_COLOR}}', statusColor)
                .replace('{{BRANCH_NAME}}', env.BRANCH_NAME ?: 'N/A')
                .replace('{{BUILD_URL}}', env.BUILD_URL)
                .replace('{{VERSION}}', env.VERSION ?: 'N/A')
                .replace('{{REPO_NAME}}', env.REPO_NAME ?: 'N/A')
                .replace('{{DURATION}}', currentBuild.durationString.replace(' and counting', ''))
        } else {
            // Fallback to simple HTML
            emailBody = """
            <html>
            <body>
                <h2 style="color: ${statusColor};">Build ${buildStatus}</h2>
                <ul>
                    <li><b>Job:</b> ${env.JOB_NAME}</li>
                    <li><b>Build Number:</b> ${env.BUILD_NUMBER}</li>
                    <li><b>Branch:</b> ${env.BRANCH_NAME}</li>
                    <li><b>Version:</b> ${env.VERSION ?: 'N/A'}</li>
                    <li><b>Duration:</b> ${currentBuild.durationString.replace(' and counting', '')}</li>
                    <li><b>Build URL:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></li>
                </ul>
                ${buildStatus == 'FAILURE' ? '<p style="color: red;"><b>Build failed. Please check the console output for details.</b></p>' : ''}
            </body>
            </html>
            """
        }
    } catch (err) {
        echo "Error reading email template: ${err}"
    }

    // Send email
    try {
        emailext(
            subject: subject,
            body: emailBody,
            mimeType: 'text/html',
            to: "${env.NOTIFICATION_EMAIL ?: 'dev-team@example.com'}",
            attachLog: buildStatus == 'FAILURE'
        )
        echo "Email notification sent successfully"
    } catch (err) {
        echo "Failed to send email notification: ${err}"
    }
}

def sendSlackNotification() {
    // Optional: Slack integration
    def buildStatus = currentBuild.result ?: 'SUCCESS'
    def statusColor = buildStatus == 'SUCCESS' ? 'good' : 'danger'
    def statusIcon = buildStatus == 'SUCCESS' ? ':white_check_mark:' : ':x:'

    try {
        slackSend(
            color: statusColor,
            message: "${statusIcon} Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}\nBranch: ${env.BRANCH_NAME}\n<${env.BUILD_URL}|View Build>"
        )
    } catch (err) {
        echo "Slack notification not configured or failed: ${err}"
    }
}

return this
