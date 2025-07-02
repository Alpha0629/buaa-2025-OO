import java.util.HashMap;

public class DispatchThread extends Thread {
    private final RequestTable mainRequestTable;
    private final HashMap<Integer, RequestTable> subRequestTable;

    public DispatchThread(RequestTable mainTable, HashMap<Integer, RequestTable> subTable) {
        this.mainRequestTable = mainTable;
        this.subRequestTable = subTable;
    }

    @Override
    public void run() {
        while (true) {
            if (mainRequestTable.getRequestCount() == 0 && mainRequestTable.isEnd()) {
                for (RequestTable requestTable : subRequestTable.values()) {
                    requestTable.setEnd();
                }
                //System.out.println("Dispatch thread end");
                break;
            }
            Person person = mainRequestTable.pullRequest();
            if (person == null) {
                continue;
            }
            dispatch(person);
        }
    }

    private void dispatch(Person person) {
        subRequestTable.get(person.getElevatorId()).pushRequest(person);
    }
}
