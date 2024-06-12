package fr.ensisa.hassenforder.tp.client.network;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import fr.ensisa.hassenforder.network.BasicAbstractReader;
import fr.ensisa.hassenforder.tp.client.model.Credential;
import fr.ensisa.hassenforder.tp.client.model.Participant;
import fr.ensisa.hassenforder.tp.client.model.Role;
import fr.ensisa.hassenforder.tp.client.model.SharedText;
import fr.ensisa.hassenforder.tp.client.model.TextOperation;
import fr.ensisa.hassenforder.tp.client.model.TextOperation.What;
import fr.ensisa.hassenforder.tp.client.model.User;
import fr.ensisa.hassenforder.tp.network.Protocol;

public class TPReader extends BasicAbstractReader {

	private User user;
	private Collection<SharedText> allTexts;
	private SharedText text;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM hh:mm:ss");
	private Collection<TextOperation> allOperations;
	private Collection<Participant> allParticipants;
	private Collection<User> allUsers;
	private Credential credential;

    public TPReader(InputStream inputStream) {
        super(inputStream);
    }

    private void eraseFields() {
    	user = null;
    	text = null;
    	credential = null;
    	allUsers = new ArrayList<>();
    	allTexts = new ArrayList<>();
    	allOperations = new ArrayList<>();
    }


    public void receive() {
        type = readInt();
        eraseFields();
        switch (type) {
        case Protocol.REPLY_KO: break;
        case Protocol.REPLY_OK: break;
        case Protocol.REPLY_NOWAY: break;
        case Protocol.REPLY_USER:
        	readUser();
        	break;
        case Protocol.REPLY_CREDENTIAL:
        	readCredential();
        	break;
        case Protocol.REPLY_ALL_TEXTS:
			readAllTexts();
        	break;
        case Protocol.REPLY_TEXT:
        	readText();
        	break;
        case Protocol.REPLY_ALL_OPERATIONS:
        	readAllOperations();
        	break;
        case Protocol.REPLY_ALL_PARTICIPANTS:
        	readAllParticipants();
        	break;
        case Protocol.REPLY_ALL_USERS:
        	readAllUsers();
        	break;
        }
    }


	private void readUser() {
		long id = readLong();
        String name = readString();
        String token = readString();
        user = new User(id, name, token);
	}

	private void readCredential() {
		long id = readLong();
		String name = readString();
		String email = readString();
		String password = readString();
		credential = new Credential(id, name, email, password);
	}

	public User getUser() {
		return this.user;
	}

	public Credential getCredential(){
		return credential;
	}

	private void readAllTexts() {
		System.out.println("Started reading all texts (client)");

		int size = readInt();
		for (int i=0; i<size; i++){
			allTexts.add(instantiateText());
		}

		System.out.println("Finished reading all texts (client). Size: " + allTexts.size());
	}

	private void readText(){
		System.out.println("Started reading a text (client)");
		text = instantiateText();
		System.out.println("Finished reading a text (client).");
	}

	private void readAllOperations() {
		System.out.println("Started reading all text operations (client)");

		int size = readInt();
		for (int i=0; i<size; i++){
			allOperations.add(instantiateOperation());
		}

		System.out.println("Finished reading all text operations (client). Size: " + allOperations.size());
	}

	private TextOperation instantiateOperation() {
		long id = readLong();
		Date date = new Date(readLong());
		String who = readString();
		String text = readString();
		What what = What.valueOf(readString());
		int where = readInt();
		return new TextOperation(id, date, who, what, where, text);
	}

	private SharedText instantiateText() {
		long id = readLong();
		Date created = new Date(readLong());
		String content = readString();
		Role role = new Role(readInt());
		String owner = readString();
		return new SharedText(id, created, content, role, owner);
	}

	public Collection<SharedText> getAllTexts() {
		return allTexts;
	}

	public SharedText getText() {
		return text;
	}

	public Collection<TextOperation> getAllTextOperations() {
		return allOperations;
	}

	private void readAllParticipants() {
		System.out.println("Started reading all text participants (client)");
		allParticipants = new ArrayList<>();

		int size = readInt();
		for (int i=0; i<size; i++){
			allParticipants.add(instantiateParticipant());
		}

		System.out.println("Finished reading all text participants (client). Size: " + allParticipants.size());
	}

	private Participant instantiateParticipant() {
		long id = readLong();
		String name = readString();
		Role role = new Role(readInt());
		return new Participant(id, name, role);
	}

	public Collection<Participant> getAllTextParticipants() {
		return allParticipants;
	}

	private void readAllUsers() {
		System.out.println("Started reading all users (client)");
		int size = readInt();
		for (int i=0; i<size; i++){
			User user = new User(readLong(), readString(), null);
			allUsers.add(user);
		}
		System.out.println("Finished reading all users (client). Size: " + allUsers.size());
	}

	public Collection<User> getAllUsers(){
		return allUsers;
	}

}
