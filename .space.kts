job("00 - Generate license request") {
    // This job needs to be run only once.
    // Set the UNITY_USERNAME and UNITY_PASSWORD secrets to generate an activation file.
    //
    // After run:
    // 1. The activation file is echoed to build output, and should be saved as *.alf
    // 2. Visit https://license.unity3d.com/manual"
    // 3. Upload the file in the form"
    // 4. Answer questions (unity pro vs personal edition, both will work, just pick the one you use)"
    // 5. Download 'Unity_v2019.x.ulf' file
    // 6. Copy the content of 'Unity_v2019.x.ulf' license file to the secret UNITY_LICENSE

    startOn {
        gitPush { enabled = false }
    }
    
    container(displayName = "Unity", image = "unityci/editor:ubuntu-2020.3.27f1-android-0.17.0") {
        resources {
            cpu = 2.cpu
            memory = 8.gb
        }
        
        env.set("UNITY_USERNAME", Secrets("unity_username"))
        env.set("UNITY_PASSWORD", Secrets("unity_password"))
        
        shellScript {
            content = """
                export BUILD_TARGET=Android
                export BUILD_PATH=${'$'}UNITY_DIR/Builds/${'$'}BUILD_TARGET/
				mkdir -p ${'$'}BUILD_PATH
                
                ${'$'}{UNITY_EXECUTABLE:-xvfb-run --auto-servernum --server-args='-screen 0 640x480x24' unity-editor} \
                  -batchmode \
                  -nographics \
                  -username "${'$'}UNITY_USERNAME" -password "${'$'}UNITY_PASSWORD" \
                  -logFile /dev/stdout
                
                cat ./unity-output.log | grep 'LICENSE SYSTEM .* Posting *' | sed 's/.*Posting *//' > "/tmp/request.txt"
                
                echo "----------------------"
                cat "/tmp/request.txt"
                echo "----------------------"

            """.trimIndent()
        }
    }
}

job("10 - Build TowerDefense") {
    container(displayName = "Unity", image = "unityci/editor:ubuntu-2020.3.27f1-android-0.17.0") {
        resources {
            cpu = 4.cpu
            memory = 12.gb
        }
        
        env.set("UNITY_LICENSE", Secrets("unity_license"))
        
        // https://josusb.com/en/blog/building-unity-on-the-command-line/
        // https://gitlab.com/game-ci/unity3d-gitlab-ci-example/-/blob/main/ci/build.sh
        shellScript {
            content = """
                ## Write license
                if [ -n "${'$'}UNITY_LICENSE" ]
                then
                    echo "Writing '\${'$'}UNITY_LICENSE' to license file /root/.local/share/unity3d/Unity/Unity_lic.ulf"
                    echo "${'$'}{UNITY_LICENSE}" | tr -d '\r' > /root/.local/share/unity3d/Unity/Unity_lic.ulf
                else
                    echo "'UNITY_LICENSE' environment variable not found"
                fi

                ## Build
                export BUILD_TARGET=Android
                export BUILD_PATH=${'$'}UNITY_DIR/Builds/${'$'}BUILD_TARGET/
				mkdir -p ${'$'}BUILD_PATH
                
                ${'$'}{UNITY_EXECUTABLE:-xvfb-run --auto-servernum --server-args='-screen 0 640x480x24' unity-editor} \
                  -projectPath ./ \
                  -quit \
                  -batchmode \
                  -nographics \
                  -buildTarget ${'$'}BUILD_TARGET \
                  ${'$'}BUILD_PATH \
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
            """.trimIndent()
        }
    }
}