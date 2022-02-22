job("Build TowerDefense") {
    container(displayName = "Unity", image = "unityci/editor:ubuntu-2020.3.27f1-android-0.17.0") {
        resources {
            cpu = 4.cpu
            memory = 12.gb
        }
        
        // https://josusb.com/en/blog/building-unity-on-the-command-line/
        // https://gitlab.com/game-ci/unity3d-gitlab-ci-example/-/blob/main/ci/build.sh
        shellScript {
            content = """
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