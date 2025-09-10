@Library('pipeline-build') _  // This line loads your shared library
def executePipeline() {
    
    environment {
            PYTHON_VERSION = '3.10'
        }

    stages {

        stage('init') {
             // Clear the workspace
                    deleteDir()
        }
        stage('Checkout') {
            steps {
                echo "Checking out branch or tag: main from repo: git@github.com/marahateeq/pipeline-build.git"
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "main"]],
                        userRemoteConfigs: [[url: "git@github.com/marahateeq/pipeline-build.git", credentialsId: "${env.GIT_CREDENTIALS}"]]
                    ])

                // Extract the repository name from the GIT URL
                    echo "Extracting repository name from GIT URL: ${env.GIT_URL}"
                    env.REPO_NAME = sh(
                        script: "basename -s .git ${env.GIT_URL}",
                        returnStdout: true
                    ).trim()
                    echo "Repository Name: ${env.REPO_NAME}"

                // List files in the workspace
                    echo "Workspace content after checkout:"
                    sh 'ls -l'
            }
        }
        stage('Setup Python') {
            steps {
                sh "python${PYTHON_VERSION} -m venv venv"
                sh ". venv/bin/activate"
                sh "pip install --upgrade pip"
                sh "pip install -r requirements.txt"
                
                
                echo "Python virtual environment setup completed."
            }
        }
        stage('Lint') {
            steps {
                sh ". venv/bin/activate && pip install flake8 && flake8 ."

                
            }
        }
        stage('Test') {
            steps {
                sh ". venv/bin/activate && pip install pytest && pytest"
            }
        }
    }
  
}