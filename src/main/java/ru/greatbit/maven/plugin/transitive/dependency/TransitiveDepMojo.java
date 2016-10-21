package ru.greatbit.maven.plugin.transitive.dependency;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by azee on 27.05.15.
 */
@Mojo(name = "transitive-dependency", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class TransitiveDepMojo extends AbstractMojo{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter
    boolean failOnError;

    final Map<String, Set<String>> artifactsMap = new HashMap<String, Set<String>>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info( "Starting trasitive dependecies analysis" );
        for (Artifact artifact : project.getArtifacts()){
            addValue(artifactsMap, artifact);
        }
        analyse();
    }

    private void analyse() {
        for (Map.Entry<String, Set<String>> entry : artifactsMap.entrySet()){
            if (entry.getValue().size() > 1){
                getLog().warn(getErrorMessage(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void addValue(Map<String, Set<String>> artifactsMap, Artifact artifact) throws MojoExecutionException {
        String key = getArtifatcKey(artifact);
        if (!artifactsMap.containsKey(key)){
            artifactsMap.put(key, new HashSet<String>());
        }
        if (artifactsMap.get(key).add(artifact.getVersion())){
            onArtifactAdded(key, artifactsMap.get(key));
        }
    }

    private void onArtifactAdded(String key, Set<String> versions) throws MojoExecutionException {
        if (versions.size() > 1 && failOnError){
            throw new MojoExecutionException(getErrorMessage(key, versions));
        }
    }

    private String getErrorMessage(String key, Set<String> versions) {
        return String.format("Found dependency collision for %s, got versions %s", key, versions);
    }

    private String getArtifatcKey(Artifact artifact){
        return String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId());
    }
}
