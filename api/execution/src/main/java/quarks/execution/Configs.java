package quarks.execution;

/**
 * Configuration property names.
 * 
 * Configuration is passed as a JSON collection of name/value
 * pairs when an executable is 
 * {@linkplain quarks.execution.Submitter#submit(Object, com.google.gson.JsonObject) submitted}.
 * <p>
 * The configuration JSON representation is summarized in the following table:
 *
 * <table border=1 cellpadding=3 cellspacing=1>
 * <caption>Summary of configuration properties</caption>
 * <tr>
 *    <td align=center><b>Attribute name</b></td>
 *    <td align=center><b>Type</b></td>
 *    <td align=center><b>Description</b></td>
 *  </tr>
 * <tr>
 *    <td>{@link #JOB_NAME}</td>
 *    <td>String</td>
 *    <td>The name of the job.</td>
 *  </tr>
 * </table>
 * </p>
 */
public interface Configs {
    /**
     * JOB_NAME is used to identify the submission configuration property 
     * containing the job name.
     * The value is {@value}.
     */
    static final String JOB_NAME = "jobName";
}
