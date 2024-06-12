package fr.ensisa.hassenforder.tp.server.network;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import fr.ensisa.hassenforder.network.BasicAbstractReader;
import fr.ensisa.hassenforder.tp.database.Operation;
import fr.ensisa.hassenforder.tp.database.Participant;
import fr.ensisa.hassenforder.tp.database.Role;
import fr.ensisa.hassenforder.tp.database.User;
import fr.ensisa.hassenforder.tp.database.UserTable;
import fr.ensisa.hassenforder.tp.database.What;
import fr.ensisa.hassenforder.tp.network.Protocol;

public class TCPReader extends BasicAbstractReader {

	private String email;
	private String password;
	private String token;
	private long whoId;
    private Credential credential;
    private String name;
    private long textId;
	private String content;
	private Collection<Participant> toSaveParticipants;
	private Collection<Operation> toSaveOperations;

	public TCPReader(InputStream inputStream) {
		super(inputStream);
	}

	private void eraseFields() {
		email = null;
		password = null;
		token = null;
		whoId = 0;
		credential = null;
		name = null;
		textId = 0;
		content = null;
		toSaveParticipants = new ArrayList<>();
		toSaveOperations = new ArrayList<>();
	}

	public void receive() {
		type = readInt();
		eraseFields();
		switch(type) {
		case Protocol.REQUEST_CONNECT:
			System.out.println("Received REQUEST_CONNECT");
			email = readString();
			password = readString();
			break;
		case Protocol.REQUEST_CREATE_USER:
			System.out.println("Received REQUEST_CREATE_USER");
			name = readString();
			email = readString();
			password = readString();
			credential = new Credential(name, email, password);
			break;
		case Protocol.REQUEST_CREDENTIAL:
			System.out.println("Received REQUEST_CREDENTIAL");
			token = readString();
			whoId = readLong();
			break;
		case Protocol.REQUEST_UPDATE_USER:
			System.out.println("Received REQUEST_UPDATE_USER");
			token = readString();
			name = readString();
			email = readString();
			password = readString();
			credential = new Credential(name, email, password);
			break;
		case Protocol.REQUEST_GET_ALL_TEXTS:
			System.out.println("Received REQUEST_GET_ALL_TEXTS");
			token = readString();
			whoId = readLong();
			break;
		case Protocol.REQUEST_GET_TEXT:
			System.out.println("Received REQUEST_GET_TEXT");
			token = readString();
			textId = readLong();
			whoId = readLong();
			break;
		case Protocol.REQUEST_NEW_TEXT:
			System.out.println("Received REQUEST_NEW_TEXT");
			token = readString();
			whoId = readLong();
			break;
		case Protocol.REQUEST_PUT_TEXT_CONTENT:
			System.out.println("Received REQUEST_PUT_TEXT_CONTENT");
			token = readString();
			textId = readLong();
			whoId = readLong();
			content = readString();
			break;
		case Protocol.REQUEST_DELETE_TEXT:
			System.out.println("Received REQUEST_DELETE_TEXT");
			token = readString();
			textId = readLong();
			whoId = readLong();
			break;
		case Protocol.REQUEST_GET_ALL_OPERATIONS:
			System.out.println("Received REQUEST_GET_ALL_OPERATIONS");
			token = readString();
			textId = readLong();
			whoId = readLong();
			break;
		case Protocol.REQUEST_GET_ALL_PARTICIPANTS:
			System.out.println("Received REQUEST_GET_ALL_PARTICIPANTS");
			token = readString();
			textId = readLong();
			whoId = readLong();
			break;
		case Protocol.REQUEST_PUT_ALL_PARTICIPANTS:
			System.out.println("Received REQUEST_PUT_ALL_PARTICIPANTS");
			token = readString();
			textId = readLong();
			whoId = readLong();
			int size = readInt();
			for (int i=0; i<size; i++){
				Role role = new Role(readInt());
				long who = readLong();
				toSaveParticipants.add(new Participant(who, textId, role));
			}
			break;
		case Protocol.REQUEST_GET_ALL_USERS:
			System.out.println("Received REQUEST_GET_ALL_USERS");
			token = readString();
			break;
		case Protocol.REQUEST_PUT_ALL_OPERATIONS:
			System.out.println("Received REQUEST_PUT_ALL_OPERATIONS");
			token = readString();
			textId = readLong();
			whoId = readLong();
			int size1 = readInt();
			for (int i=0; i<size1; i++){
				Date date = new Date(readLong());
				String text = readString();
				What what = What.valueOf(readString().toUpperCase());
				int where = readInt();
				toSaveOperations.add(new Operation(date, whoId, textId, what, where, text));
			}
			break;
		}
    }

	public String getEmail(){
		return email;
	}

	public String getPassword(){
		return password;
	}

	public Credential getCredential() {
        return credential;
    }

    public String getName() {
        return name;
    }

	public String getToken(){
		return token;
	}

	public long getWhoId(){
		return whoId;
	}

	public long getTextId(){
		return textId;
	}

	public String getContent() {
		return content;
	}

	public Collection<Participant> getParticipants(){
		return toSaveParticipants;
	}

	public Collection<Operation> getOperations(){
		return toSaveOperations;
	}

}
