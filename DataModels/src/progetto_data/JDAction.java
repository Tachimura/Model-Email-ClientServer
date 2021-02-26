package progetto_data;

/**
 *
 * @author gianluca
 */
public class JDAction {

    private final String action;

    public JDAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return action;
    }
}