# Pipeline Build Library

Shared Jenkins pipeline library for building Python and Frontend applications.

## Overview

This repository contains reusable Jenkins pipeline scripts for:
- **Python Services**: Build, test, package, and containerize Python applications
- **Frontend Applications**: Build, test, and containerize HTML/JS/CSS applications

## Features

### Python Pipeline (`Jenkinsfile.python`)
- Python 3.11 environment setup
- Virtual environment management
- Dependency installation
- Unit test execution with pytest
- PyInstaller packaging
- Docker image creation and push
- Artifact archival
- Version tagging from Git tags
- Email notifications

### Frontend Pipeline (`Jenkinsfile.frontend`)
- Node.js environment setup (v18)
- NPM dependency management
- Frontend build process
- Test execution
- Docker image creation with Nginx
- Docker registry push
- Static file support
- Email notifications

### Notification System (`vars/notify.groovy`)
- Build start notifications
- Build completion notifications
- Success/failure email alerts
- HTML email templates
- Console log attachment on failure
- Optional Slack integration

## Usage

### 1. Configure Jenkins Shared Library

In Jenkins:
1. Go to **Manage Jenkins** → **Configure System**
2. Scroll to **Global Pipeline Libraries**
3. Add a new library:
   - **Name**: `pipeline-build`
   - **Default version**: `main`
   - **Retrieval method**: Modern SCM
   - **Source Code Management**: Git
   - **Project Repository**: `path/to/pipeline-build`

### 2. Use in Service Jenkinsfile

For Python services:

```groovy
@Library('pipeline-build') _

pipeline {
    agent any

    environment {
        APP_NAME = 'user-api'
        GIT_URL = 'https://github.com/example/user-api.git'
        BRANCH_NAME = "${env.BRANCH_NAME}"
        NOTIFICATION_EMAIL = 'team@example.com'
    }

    stages {
        stage('Build') {
            steps {
                script {
                    def pythonPipeline = load "${env.WORKSPACE}/../pipeline-build/Jenkinsfile.python"
                    pythonPipeline.executePipeline()
                }
            }
        }
    }
}
```

For Frontend applications:

```groovy
@Library('pipeline-build') _

pipeline {
    agent any

    environment {
        APP_NAME = 'product-frontend'
        GIT_URL = 'https://github.com/example/product-frontend.git'
        BRANCH_NAME = "${env.BRANCH_NAME}"
        NOTIFICATION_EMAIL = 'team@example.com'
    }

    stages {
        stage('Build') {
            steps {
                script {
                    def frontendPipeline = load "${env.WORKSPACE}/../pipeline-build/Jenkinsfile.frontend"
                    frontendPipeline.executePipeline()
                }
            }
        }
    }
}
```

## Environment Variables

### Required
- `APP_NAME`: Name of the application
- `GIT_URL`: Git repository URL
- `BRANCH_NAME`: Branch or tag to build

### Optional
- `PYTHON_VERSION`: Python version (default: 3.11)
- `NODE_VERSION`: Node.js version (default: 18)
- `DOCKER_REGISTRY`: Docker registry URL (default: localhost:5000)
- `NOTIFICATION_EMAIL`: Email for notifications (default: dev-team@example.com)
- `BUILD_ARTIFACTS_PATH`: Path for build artifacts (default: /opt/build-artifacts)

## Versioning

The pipeline automatically determines the version:
- **Tagged builds**: Uses the tag name as version (e.g., `1.0.0`, `v2.1.3`)
- **Branch builds**: Uses short Git commit hash (e.g., `a1b2c3d`)

## Docker Registry

By default, the pipeline uses `localhost:5000` as the Docker registry. Update the `DOCKER_REGISTRY` environment variable to use your own registry:

```groovy
environment {
    DOCKER_REGISTRY = 'your-registry.example.com'
}
```

For AWS ECR:
```groovy
environment {
    DOCKER_REGISTRY = '123456789.dkr.ecr.us-east-1.amazonaws.com'
}
```

## Email Notifications

Email notifications are sent at:
1. **Build start**: Basic information about the triggered build
2. **Build completion**: Detailed results with status, duration, and links

Customize the email template by editing `resources/email-template.html`.

## Requirements

### Jenkins Plugins
- Git Plugin
- Pipeline Plugin
- Docker Pipeline Plugin
- Email Extension Plugin
- Workspace Cleanup Plugin

### Build Agents
- Git
- Docker
- Python 3.11+ (for Python builds)
- Node.js 18+ (for frontend builds)

## File Structure

```
pipeline-build/
├── Jenkinsfile.python          # Python build pipeline
├── Jenkinsfile.frontend        # Frontend build pipeline
├── vars/
│   └── notify.groovy          # Notification functions
├── resources/
│   └── email-template.html    # Email template
└── README.md                  # This file
```

## Troubleshooting

### Build fails to find Git
Ensure Git is installed on the Jenkins agent and available in PATH.

### Docker commands fail
Ensure the Jenkins user has permission to run Docker commands:
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Email notifications not working
Check Jenkins Email Extension plugin configuration:
1. Go to **Manage Jenkins** → **Configure System**
2. Configure **Extended E-mail Notification**
3. Set SMTP server and credentials

## Contributing

To add new features or pipelines:
1. Create new pipeline files following the naming convention `Jenkinsfile.<type>`
2. Update this README with usage instructions
3. Test thoroughly before merging

## License

Internal use only - proprietary
