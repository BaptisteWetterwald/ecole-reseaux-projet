package fr.ensisa.hassenforder.tp.client.network;

import java.io.OutputStream;
import java.util.Collection;

import fr.ensisa.hassenforder.tp.client.model.Credential;
import fr.ensisa.hassenforder.tp.network.Protocol;
import fr.ensisa.hassenforder.network.BasicAbstractWriter;

public class TPWriter extends BasicAbstractWriter {

    public TPWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public void connect(String email, String passwd) {
        writeInt(Protocol.REQUEST_CONNECT);
        writeString(email);
        writeString(passwd);
    }

    public void createUser(Credential credential) {
		writeInt(Protocol.REQUEST_CREATE_USER);
		writeString(credential.getName());
		writeString(credential.getMail());
		writeString(credential.getPasswd());
	}

	public void getCredential(String token, long whoId) {
		writeInt(Protocol.REQUEST_CREDENTIAL);
		writeString(token);
		writeLong(whoId);
	}

	public void updateUser(String token, Credential credential) {
		writeInt(Protocol.REQUEST_UPDATE_USER);
		writeString(token);
		writeString(credential.getName());
		writeString(credential.getMail());
		writeString(credential.getPasswd());
	}

	public void getAllTexts(String token, long who) {
		writeInt(Protocol.REQUEST_GET_ALL_TEXTS);
		writeString(token);
		writeLong(who);
	}

	public void getText(String token, long textId, long whoId) {
		writeInt(Protocol.REQUEST_GET_TEXT);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
	}

	public void newText(String token, long creatorId) {
		writeInt(Protocol.REQUEST_NEW_TEXT);
		writeString(token);
		writeLong(creatorId);
	}

	public void saveTextContent(String token, long textId, long whoId, String content) {
		writeInt(Protocol.REQUEST_PUT_TEXT_CONTENT);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
		writeString(content);
	}

	public void deleteText(String token, long textId, long whoId) {
		writeInt(Protocol.REQUEST_DELETE_TEXT);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
	}

	public void getAllTextOperations(String token, long textId, long whoId) {
		writeInt(Protocol.REQUEST_GET_ALL_OPERATIONS);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
	}

	public void getAllTextParticipants(String token, long textId, long whoId) {
		writeInt(Protocol.REQUEST_GET_ALL_PARTICIPANTS);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
	}

	public void saveTextParticipants(String token, long textId, long whoId,
			Collection<ParticipantOperationRequest> toSave) {
		writeInt(Protocol.REQUEST_PUT_ALL_PARTICIPANTS);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
		int size = toSave.size();
		writeInt(size);
		for (ParticipantOperationRequest req : toSave){
			writeInt(req.getRole().asInt());
			writeLong(req.getWho());
		}
	}

	public void getAllUsers(String token) {
		writeInt(Protocol.REQUEST_GET_ALL_USERS);
		writeString(token);
	}

	public void saveTextOperations(String token, long textId, long whoId, Collection<TextOperationRequest> toSave) {
		writeInt(Protocol.REQUEST_PUT_ALL_OPERATIONS);
		writeString(token);
		writeLong(textId);
		writeLong(whoId);
		int size = toSave.size();
		writeInt(size);
		for (TextOperationRequest req : toSave){
			writeLong(req.getDate().getTime());
			writeString(req.getText());
			writeString(req.getWhat().name());
			writeInt(req.getWhere());
		}
	}





}
