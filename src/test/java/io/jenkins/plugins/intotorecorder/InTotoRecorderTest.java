package io.jenkins.plugins.intotorecorder;

import hudson.Launcher;
import hudson.model.*;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Debayan Ghosh
 *
 */

public class InTotoRecorderTest{

    private InTotoRecorder recorder;
    
    private final String keyFilepath = "src/test/resources/keys/somekey.pem";

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testConstructor() {
        
        // Test constructor with empty credentialId and keyFilepath
        recorder = new InTotoRecorder("", keyFilepath, "stepName", "transport");
        assertEquals("", recorder.getCredentialId());
        assertEquals(keyFilepath, recorder.getKeyPath());
        assertEquals("stepName", recorder.getStepName());
        assertEquals("transport", recorder.getTransport());

        // Test constructor with empty stepName, empty credentialId and keyFilepath
        recorder = new InTotoRecorder("", keyFilepath, "", "transport");
        assertEquals("step", recorder.getStepName());
    }

    @Test
    public void testRecorderInFreeStyleProject() throws Exception {
        
        // Test recorder in FreeStyleProject with empty credentialId and keyFilepath
        FreeStyleBuild build;
        FreeStyleProject project = j.createFreeStyleProject("test");
        recorder = new InTotoRecorder("", keyFilepath, "stepName", "transport");
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
                return recorder.prebuild(build, listener);
            }
        });
        build = project.scheduleBuild2(0).get();
        j.assertBuildStatusSuccess(build);
        j.jenkins.getQueue().clear();
    }
    
}
