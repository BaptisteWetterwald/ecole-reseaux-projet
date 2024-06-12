package fr.ensisa.hassenforder.tp.server.network;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import fr.ensisa.hassenforder.tp.database.Role;
import fr.ensisa.hassenforder.tp.database.SharedText;
import fr.ensisa.hassenforder.tp.database.User;
import fr.ensisa.hassenforder.tp.network.Protocol;
import fr.ensisa.hassenforder.network.BasicAbstractWriter;

public class TCPWriter extends BasicAbstractWriter {

    public TCPWriter(OutputStream outputStream) {
        super (outputStream);
    }

    public void createOK() {
        writeInt(Protocol.REPLY_OK);
    }

    public void createKO() {
        writeInt(Protocol.REPLY_KO);
    }

    public void createConnectedUser(User user, String token) {
		writeInt(Protocol.REPLY_USER);
		writeLong(user.getId());
		writeString(user.getName());
		writeString(token);
		System.out.println("Sent a user (from server)");
	}

    public void createCredential(long id, Credential credential) {
		writeInt(Protocol.REPLY_CREDENTIAL);
		writeString(credential.getName());
		writeString(credential.getMail());
		writeString(credential.getPasswd());
		System.out.println("Sent a credential (from server)");
	}

	public void createAllTexts(Collection<SharedTextReply> allTextsReply){
		this.writeInt(Protocol.REPLY_ALL_TEXTS);
		int size = allTextsReply.size();
		writeInt(size);
		for (SharedTextReply textReply : allTextsReply){
			writeTextReply(textReply);
		}
		System.out.println("Sent all texts, size: " + size + " (from server)");
	}

	public void createText(SharedTextReply textReply) {
		writeInt(Protocol.REPLY_TEXT);
		writeTextReply(textReply);
		System.out.println("Sent a text (from server)");
	}

	private void writeTextReply(SharedTextReply textReply){
		writeLong(textReply.getId());
		writeLong(textReply.getDate().getTime());
		writeString(textReply.getContent());
		writeInt(textReply.getRole().asInt());
		writeString(textReply.getOwner());
	}

	public void createAllTextOperations(Collection<OperationReply> allOperationsReply) {
		writeInt(Protocol.REPLY_ALL_OPERATIONS);
		int size = allOperationsReply.size();
		writeInt(size);
		for (OperationReply reply : allOperationsReply){
			writeOperationReply(reply);
		}
		System.out.println("Sent all text operations, size: " + size + " (from server)");
	}

	private void writeOperationReply(OperationReply reply) {
		writeLong(reply.getId());
		writeLong(reply.getDate().getTime());
		writeString(reply.getName());
		writeString(reply.getText());
		writeString(reply.getWhat().name());
		writeInt(reply.getWhere());
	}

	public void createAllTextParticipants(Collection<ParticipantReply> allParticipantsReply) {
		writeInt(Protocol.REPLY_ALL_PARTICIPANTS);
		int size = allParticipantsReply.size();
		writeInt(size);
		for (ParticipantReply reply : allParticipantsReply){
			writeParticipantReply(reply);
		}
		System.out.println("Sent all text participants, size: " + size + " (from server)");
	}

	private void writeParticipantReply(ParticipantReply reply) {
		writeLong(reply.getId());
		writeString(reply.getName());
		writeInt(reply.getRole().asInt());
	}

	public void createAllUsers(Collection<User> users) {
		writeInt(Protocol.REPLY_ALL_USERS);
		int size = users.size();
		writeInt(size);
		for (User user : users){
			writeLong(user.getId());
			writeString(user.getName());
		}
		System.out.println("Sent all users, size: " + size + " (from server)");
	}






}
