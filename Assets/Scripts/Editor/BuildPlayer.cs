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
            Build("artifacts/towerdefense-linux-x64", BuildTarget.StandaloneLinux64);
        }
        
        [MenuItem("Services/Build/Build macOS (OSX)")]
        public static void MacOsBuild()
        {
            Build("artifacts/towerdefense-ios", BuildTarget.StandaloneOSX);
        }
        
        [MenuItem("Services/Build/Build Windows x64")]
        public static void WindowsBuild()
        {
            Build("artifacts/towerdefense-windows-x64", BuildTarget.StandaloneWindows64);
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