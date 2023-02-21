job("00 - Generate license request") {
    // This job needs to be run only once.
    // Set the UNITY_USERNAME and UNITY_PASSWORD secrets to generate an activation file.
    //
    // After run:
    // 1. The activation file is echoed to build output, and should be saved as *.alf
    // 2. Visit https://license.unity3d.com/manual
    // 3. Upload the file in the form
    // 4. Answer questions (unity pro vs personal edition, both will work, just pick the one you use)
    // 5. Download 'Unity_v2021.x.ulf' file
    // 6. Copy the content of 'Unity_v2021.x.ulf' license file to the secret unity_license

    startOn {
        gitPush { enabled = false }
    }
    
    container(displayName = "Unity", image = "unityci/editor:ubuntu-2021.3.17f1-linux-il2cpp-1.0.1") {
        resources {
            cpu = 2.cpu
            memory = 8.gb
        }
        
        env.set("UNITY_USERNAME", Secrets("unity_username"))
        env.set("UNITY_PASSWORD", Secrets("unity_password"))
        
        shellScript {
            content = """                
                ${'$'}{UNITY_EXECUTABLE:-xvfb-run --auto-servernum --server-args='-screen 0 640x480x24' unity-editor} \
                  -batchmode \
                  -nographics \
                  -username "${'$'}UNITY_USERNAME" -password "${'$'}UNITY_PASSWORD" \
                  -logFile /dev/stdout | tee ./unity-output.log

                
                cat ./unity-output.log | grep 'LICENSE SYSTEM .* Posting *' | sed 's/.*Posting *//' > "/tmp/request.txt"
                
                echo "----------------------"
                echo "1. Save the below output as *.alf"
                echo "2. Visit https://license.unity3d.com/manual
                echo "3. Upload the file in the form
                echo "4. Answer questions (unity pro vs personal edition, both will work, just pick the one you use)
                echo "5. Download 'Unity_v20xx.x.ulf' file
                echo "6. Copy the content of 'Unity_v20xx.x.ulf' license file to the Space project secret unity_license
                echo "----------------------"
                cat "/tmp/request.txt"
                echo "----------------------"
            """.trimIndent()
        }
    }
}

job("Build TowerDefense - Linux x64") {

    buildUnity(
        displayName = "Build Linux x64",
        containerImage = "unityci/editor:ubuntu-2021.3.17f1-linux-il2cpp-1.0.1",
        executeMethod = "Editor.BuildPlayer.LinuxBuild",
        artifactsPath = "artifacts/linux-x64/",
        publishArtifactFileName = "towerdefense-linux-x64.tar.gz"
    )
}

job("Build TowerDefense - macOS") {

    buildUnity(
        displayName = "Build macOS",
        containerImage = "unityci/editor:2021.3.17f1-mac-mono-1.0.1",
        executeMethod = "Editor.BuildPlayer.MacOsBuild",
        artifactsPath = "artifacts/macos/",
        publishArtifactFileName = "towerdefense-macos.tar.gz"
    )
}

job("Build TowerDefense - Windows x64") {

    buildUnity(
        displayName = "Build Windows",
        containerImage = "unityci/editor:2021.3.17f1-windows-mono-1.0.1",
        executeMethod = "Editor.BuildPlayer.WindowsBuild",
        artifactsPath = "artifacts/windows-x64/",
        publishArtifactFileName = "towerdefense-windows-x64.tar.gz"
    )
}

job("Build TowerDefense - Android") {
    startOn {
        gitPush { enabled = false }
    }

    
    buildUnity(
        displayName = "Build Android",
        containerImage = "unityci/editor:2021.3.17f1-android-1.0.1",
        executeMethod = "Editor.BuildPlayer.AndroidBuild",
        artifactsPath = "artifacts/android/",
        publishArtifactFileName = "towerdefense-android.tar.gz"
    )
}

job("Build TowerDefense - iOS") {
    startOn {
        gitPush { enabled = false }
    }

    
    buildUnity(
        displayName = "Build iOS",
        containerImage = "unityci/editor:2021.3.17f1-ios-1.0.1",
        executeMethod = "Editor.BuildPlayer.IosBuild",
        artifactsPath = "artifacts/ios/",
        publishArtifactFileName = "towerdefense-ios.tar.gz"
    )
}

fun StepsScope.buildUnity(
    displayName: String,
    containerImage: String,
    executeMethod: String,
    artifactsPath: String,
    publishArtifactFileName: String
) {
    container(displayName = displayName, image = containerImage) {
        resources {
            cpu = 8.cpu
            memory = 16.gb
        }

        fileInput {
            source = FileSource.Text("{{ project:unity_license }}")
            localPath = "/mnt/space/Unity_lic.ulf"
        }

        // https://josusb.com/en/blog/building-unity-on-the-command-line/
        // https://gitlab.com/game-ci/unity3d-gitlab-ci-example/-/blob/main/ci/build.sh
        shellScript {
            content = """
                ## Initialize and copy license secret
                mkdir -p /root/.cache/unity3d
                mkdir -p /root/.local/share/unity3d/Unity/
                cp /mnt/space/Unity_lic.ulf /root/.local/share/unity3d/Unity
                
                ## Build
                unity-editor \
                  -projectPath ./ \
                  -quit \
                  -batchmode \
                  -nographics \
                  -executeMethod $executeMethod \
                  -logFile /dev/stdout
                
                UNITY_EXIT_CODE=${'$'}?
                
                if [ ${'$'}UNITY_EXIT_CODE -eq 0 ]; then
                  echo "Run succeeded, no failures occurred";
                elif [ ${'$'}UNITY_EXIT_CODE -eq 2 ]; then
                  echo "Run succeeded, some tests failed";
                elif [ ${'$'}UNITY_EXIT_CODE -eq 3 ]; then
                  echo "Run failure (other failure)";
                else
                  echo "Unexpected exit code ${'$'}UNITY_EXIT_CODE";
                fi
                
                echo Uploading artifacts...
                tar -zcvf $publishArtifactFileName $artifactsPath
                curl -i -H "Authorization: Bearer ${'$'}JB_SPACE_CLIENT_TOKEN" -F file=@"$publishArtifactFileName" https://files.pkg.jetbrains.space/demo/p/td/towerdefense/space/${'$'}JB_SPACE_EXECUTION_NUMBER/
            """.trimIndent()
        }
    }
}