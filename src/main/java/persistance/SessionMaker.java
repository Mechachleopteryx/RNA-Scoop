package persistance;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.ConsoleController;
import mediator.ControllerMediator;
import java.util.ArrayList;

public class SessionMaker {
    public static final String PATH_KEY = "path";
    public static final String ISOFORM_PLOT_OPEN_KEY = "isoform open";
    public static final String TSNE_PLOT_OPEN_KEY = "t-sne open";
    public static final String CONSOLE_OPEN_KEY = "console open";
    public static final String REVERSE_COMPLEMENT_KEY = "rev complement";
    public static final String HIDE_ISOFORMS_WITH_NO_JUNCTIONS_KEY = "hide isoforms with no junctions";
    public static final String HIDE_DOT_PLOT_KEY = "hide dot plot";
    public static final String SHOW_GENE_NAME_AND_ID_KEY = "show gene name and id";
    public static final String SHOW_GENE_NAME_KEY = "show gene name";
    public static final String SHOW_GENE_ID_KEY = "show gene id";
    public static final String SHOW_ISOFORM_NAME_KEY = "show isoform name";
    public static final String SHOW_ISOFORM_ID_KEY = "show isoform id";
    public static final String CONSOLE_MESSAGES_KEY = "console messages";
    public static final String MESSAGE_TEXT_KEY = "message text";
    public static final String MESSAGE_IS_ERROR_KEY = "message is error";

    /**
     * Creates a session containing:
     *   - the current loaded path
     *   - whether the isoform plot, t-sne plot and console are opened or closed
     *   - whether the reverse complement option is selected
     *   - whether the show names option is selected
     *   - the console messages
     */
    public static JSONObject makeSession() {
        JSONObject session = new JSONObject();
        session.put(PATH_KEY, ControllerMediator.getInstance().getCurrentLoadedPath());
        session.put(ISOFORM_PLOT_OPEN_KEY, ControllerMediator.getInstance().isIsoformPlotOpen());
        session.put(TSNE_PLOT_OPEN_KEY, ControllerMediator.getInstance().isTSNEPlotOpen());
        session.put(CONSOLE_OPEN_KEY, ControllerMediator.getInstance().isConsoleOpen());
        session.put(REVERSE_COMPLEMENT_KEY, ControllerMediator.getInstance().isReverseComplementing());
        session.put(HIDE_ISOFORMS_WITH_NO_JUNCTIONS_KEY, ControllerMediator.getInstance().isHidingIsoformsWithNoJunctions());
        session.put(HIDE_DOT_PLOT_KEY, ControllerMediator.getInstance().isHidingDotPlot());
        session.put(SHOW_GENE_NAME_AND_ID_KEY, ControllerMediator.getInstance().isShowingGeneNameAndID());
        session.put(SHOW_GENE_NAME_KEY, ControllerMediator.getInstance().isShowingGeneName());
        session.put(SHOW_GENE_ID_KEY, ControllerMediator.getInstance().isShowingGeneID());
        session.put(SHOW_ISOFORM_NAME_KEY, ControllerMediator.getInstance().isShowingIsoformName());
        session.put(SHOW_ISOFORM_ID_KEY, ControllerMediator.getInstance().isShowingIsoformID());
        addConsoleMessagesToSession(session);
        return session;
    }

    /**
     * Saves the current console messages to the given session
     */
    private static void addConsoleMessagesToSession(JSONObject session) {
        ArrayList<ConsoleController.Message> consoleMessages = ControllerMediator.getInstance().getConsoleMessages();
        JSONArray messagesArray = new JSONArray();
        for(ConsoleController.Message message : consoleMessages) {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put(MESSAGE_TEXT_KEY, message.getMessageText());
            messageJSON.put(MESSAGE_IS_ERROR_KEY, message.getMessageType());
            messagesArray.put(messageJSON);
        }
        session.put(CONSOLE_MESSAGES_KEY, messagesArray);
    }
}