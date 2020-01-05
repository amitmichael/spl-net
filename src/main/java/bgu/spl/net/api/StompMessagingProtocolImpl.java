package bgu.spl.net.api;
import bgu.spl.net.Commands.*;
import bgu.spl.net.impl.rci.Command;
import bgu.spl.net.PassiveObjects.User;
import bgu.spl.net.srv.Connections;
import java.io.Serializable;

public class StompMessagingProtocolImpl<T> implements StompMessagingProtocol<Serializable> {
    private User user;
    private int connectionid;
    private Connections connections;
    private boolean terminate;

    public StompMessagingProtocolImpl( ){
    }

    public void start(int connectionId, Connections<String> connections) {
        this.connectionid = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(Serializable message) {
        User user = connections.getClientByMsg((Message) message);
        Message msg = (Message) message;
        switch (msg.getCommand()){
            case ("CONNECT"):
                System.out.println("CONNECT");
                break;
            case ("SUBSCRIBE"): {
                Command c = new JoinGenre(msg);
                Message toSend = (Message) c.execute(user);
                connections.send(user.getConnectionId(),toSend);
                System.out.println("SUBSCRIBE");
                break;
            }
            case ("SEND"): {
                System.out.println("SEND");
                if (msg.getBody().indexOf("added") > 0) { //addBookCase
                    Command c = new AddBook(msg);
                    Message toSend = (Message)  c.execute(user);
                    connections.send(msg.getHeader().get(0).getSecond(),toSend);
                }
                else if (msg.getBody().indexOf("Returning") > 0) { //ReturnBookCase
                    Command c = new ReturnBook(msg);
                    Message toSend = (Message)  c.execute(user);
                    connections.send(msg.getHeader().get(0).getSecond(),toSend);
                }
                else if (msg.getBody().indexOf("book status") > 0) { //GenreBookStatusCase
                    String genre = msg.getHeader().get(0).getSecond();
                    for (Object tmp : connections.getUsersByTopic(genre)) { //iterate over all users subscribed to genre
                        User tmpUser = (User) tmp;
                        Command c = new GenreBookStatus(msg);
                        Message toSend = (Message) c.execute(tmp);
                        connections.send(genre, toSend);
                    }
                }
                break;
            }
            case ("DISCONNECT"):
                System.out.println("DISCONNECT");
                break;
            case ("UNSUBSCRIBE"): {
                Command c = new ExitGenre(msg);
                Message toSend = (Message)  c.execute(user);
                connections.send(user.getConnectionId(),toSend);
                System.out.println("UNSUBSCRIBE");
                break;
            }
        }
    }



    @Override
    public boolean shouldTerminate() {
        if (this.terminate ==true){
            connections.disconnect(connectionid);
        }
        return this.terminate;
    }




}
