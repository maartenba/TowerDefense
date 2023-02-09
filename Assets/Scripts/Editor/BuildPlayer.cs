using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace Editor
{
    public class BuildPlayer : MonoBehaviour
    {
        [MenuItem("Services/Build/Build Linux x64")]
        public static void LinuxBuild()
        {
            Build("artifacts/linux-x64/towerdefense", BuildTarget.StandaloneLinux64);
        }
        
        [MenuItem("Services/Build/Build macOS (OSX)")]
        public static void MacOsBuild()
        {
            Build("artifacts/macos/towerdefense", BuildTarget.StandaloneOSX);
        }
        
        [MenuItem("Services/Build/Build iOS")]
        public static void IosBuild()
        {
            Build("artifacts/ios/towerdefense", BuildTarget.iOS);
        }
        
        [MenuItem("Services/Build/Build Android")]
        public static void AndroidBuild()
        {
            Build("artifacts/android/towerdefense", BuildTarget.Android);
        }
        
        [MenuItem("Services/Build/Build Windows x64")]
        public static void WindowsBuild()
        {
            Build("artifacts/windows-x64/towerdefense", BuildTarget.StandaloneWindows64);
        }

        private static void Build(string locationPathName, BuildTarget target)
        {
            BuildPlayerOptions buildPlayerOptions = new BuildPlayerOptions();
            buildPlayerOptions.scenes = new[]
            {
                "Assets/Scenes/MainMenu.unity",
                "Assets/Scenes/Levels/Level1/Level1.unity",
                "Assets/Scenes/Levels/Level2/Level2.unity",
                "Assets/Scenes/Levels/Level3/Level3.unity",
                "Assets/Scenes/Levels/Level4/Level4.unity",
                "Assets/Scenes/Levels/Level5/Level5.unity"
            };
            buildPlayerOptions.locationPathName = locationPathName;
            buildPlayerOptions.target = target;
            buildPlayerOptions.options = BuildOptions.None;

            BuildReport report = BuildPipeline.BuildPlayer(buildPlayerOptions);
            BuildSummary summary = report.summary;

            if (summary.result == BuildResult.Succeeded)
            {
                Debug.Log("Build succeeded: " + summary.totalSize + " bytes");
            }

            if (summary.result == BuildResult.Failed)
            {
                Debug.Log("Build failed");
            }
        }
    }
}