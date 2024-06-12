package fr.ensisa.hassenforder.tp.server.network;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import fr.ensisa.hassenforder.tp.network.Protocol;
import fr.ensisa.hassenforder.tp.database.Model;
import fr.ensisa.hassenforder.tp.database.Operation;
import fr.ensisa.hassenforder.tp.database.Participant;
import fr.ensisa.hassenforder.tp.database.Role;
import fr.ensisa.hassenforder.tp.database.SharedText;
import fr.ensisa.hassenforder.tp.database.User;
import fr.ensisa.hassenforder.tp.database.What;

public class TCPSession extends Thread {

	private Socket connection;
	private Model model;

	public TCPSession(Socket connection, Model model) {
		this.connection = connection;
		this.model = model;
	}

	private SharedTextReply adaptText(SharedText input, long whoId, boolean reduceContent) {
		Collection<Participant> participants = model.getParticipants().getAllParticipants(input.getId());
		if (participants == null) return null;
		long owner = 0;
		Role role = null;
		for (Participant participant : participants) {
			if (participant.getRole().isCreator()) owner = participant.getWho();
			if (participant.getWho() == whoId) role = participant.getRole();
		}
		if (owner == 0) return null;
		if (role == null) return null;
		User user = model.getUsers().getUser(owner);
		if (user == null) return null;
		String content = input.getContent();
		if (reduceContent && content.length() > 20) content = content.substring(0, 18) + " ...";
		return new SharedTextReply(
				input.getId(),
				input.getCreated(),
				content,
				role,
				user.getName()
		);
	}

	private Collection<SharedTextReply> adaptTexts(Collection<SharedText> inputs, long whoId) {
		List<SharedTextReply> output = new ArrayList<>();
		for (SharedText input : inputs) {
			SharedTextReply text = adaptText(input, whoId, true);
			if (text != null) output.add(text);
		}
		return output;
	}

	private ParticipantReply adaptParticipant(Participant participant, String name) {
		return new ParticipantReply(participant.getWho(), name, participant.getRole());
	}

	private Collection<ParticipantReply> adaptParticipants(Collection<Participant> inputs) {
		List<ParticipantReply> output = new ArrayList<>();
		for (Participant input : inputs) {
			User user = model.getUsers().getUser(input.getWho());
			if (user == null) continue;
			ParticipantReply participant = adaptParticipant(input, user.getName());
			if (participant != null) output.add(participant);
		}
		return output;
	}

	private OperationReply adaptOperation(Operation operation, String name) {
		return new OperationReply(
				operation.getId(),
				operation.getDate(),
				name,
				operation.getWhat(),
				operation.getWhere(),
				operation.getText()
		);
	}

	private Collection<OperationReply> adaptOperations(Collection<Operation> inputs) {
		List<OperationReply> output = new ArrayList<>();
		for (Operation input : inputs) {
			User user = model.getUsers().getUser(input.getWho());
			if (user == null) continue;
			OperationReply operation = adaptOperation(input, user.getName());
			if (operation != null) output.add(operation);
		}
		return output;
	}

	public void close () {
		this.interrupt();
		try {
			if (connection != null)
				connection.close();
		} catch (IOException e) {
		}
		connection = null;
	}

	//TODO

	public boolean operate() {
		try {
			boolean result = true;
			TCPWriter writer = new TCPWriter(connection.getOutputStream());
			TCPReader reader = new TCPReader(connection.getInputStream());
			reader.receive();
			switch (reader.getType ()) {
			case 0 : result = false; break; // socket closed
			//TODO
			case Protocol.REQUEST_CONNECT: processConnect(reader, writer); break;
			case Protocol.REQUEST_CREATE_USER: processCreateUser(reader, writer); break;
			case Protocol.REQUEST_CREDENTIAL: processCredential(reader, writer); break;
			case Protocol.REQUEST_UPDATE_USER: processUpdateUser(reader, writer); break;
			case Protocol.REQUEST_GET_ALL_TEXTS: processGetAllTexts(reader, writer); break;
			case Protocol.REQUEST_GET_TEXT: processGetText(reader, writer); break;
			case Protocol.REQUEST_NEW_TEXT: processNewText(reader, writer); break;
			case Protocol.REQUEST_PUT_TEXT_CONTENT: processSaveTextContent(reader, writer); break;
			case Protocol.REQUEST_DELETE_TEXT: processDeleteText(reader, writer); break;
			case Protocol.REQUEST_GET_ALL_OPERATIONS: processGetAllTextOperations(reader, writer); break;
			case Protocol.REQUEST_GET_ALL_PARTICIPANTS: processGetAllTextParticipants(reader, writer); break;
			case Protocol.REQUEST_PUT_ALL_PARTICIPANTS: processPutAllTextParticipants(reader, writer); break;
			case Protocol.REQUEST_GET_ALL_USERS: processGetAllUsers(reader, writer); break;
			case Protocol.REQUEST_PUT_ALL_OPERATIONS: processPutAllTextOperations(reader, writer); break;
			default: result = false; // connection jammed
			}
			if (result) writer.send();
			return result;
		} catch (IOException e) {
			return false;
		}
	}

	public void run() {
		while (true) {
			if (! operate())
				break;
		}
		try {
			if (connection != null) connection.close();
		} catch (IOException e) {
		}
	}

	private void processConnect(TCPReader reader, TCPWriter writer) {
        User user = model.getUsers().getUserByMail(reader.getEmail());
        if (user == null) {
            writer.createKO();
            return;
        }

        if(user.getPasswd().equals(reader.getPassword())) {
        	String token = model.getTokens().addNewToken(user.getId());
        	writer.createConnectedUser(user, token);
        	return;
        }

        model.getTokens().removeToken(user.getId());
        writer.createKO();
    }

	private void processCreateUser(TCPReader reader, TCPWriter writer) {
		Credential credential = reader.getCredential();
		if (credential.getName().isEmpty()){
			writer.createKO();
			System.out.println("Credential name is empty");
			return;
		}

		User user = model.getUsers().getUserByName(credential.getName());
		if (user != null){
			writer.createKO();
			System.out.println("User is not null");
			return;
		}

		model.getUsers().addUser(
			new User(
				credential.getName(),
				credential.getMail(),
				credential.getPasswd()
			)
		);
		writer.createOK();
	}

	private void processCredential(TCPReader reader, TCPWriter writer) {
		if (!model.getTokens().isKnown(reader.getToken())){
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		User user = model.getUsers().getUser(reader.getWhoId());
		if (user == null){
			writer.createKO();
			System.out.println("User is null");
			return;
		}

		writer.createCredential(user.getId(), new Credential(user.getName(), user.getMail(), user.getPasswd()));
	}

	private void processUpdateUser(TCPReader reader, TCPWriter writer) {
		if (!model.getTokens().isKnown(reader.getToken())){
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Credential credential = reader.getCredential();
		if (credential.getName().isEmpty()){
			writer.createKO();
			System.out.println("Credential name is empty");
			return;
		}

		User user = model.getUsers().getUserByName(credential.getName());
		if (user == null){
			writer.createKO();
			System.out.println("User is null");
			return;
		}

		user.setMail(credential.getMail());
		user.setPasswd(credential.getPasswd());
		writer.createOK();
	}

	private void processGetAllTexts(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing GET_ALL_TEXTS");
		String token = reader.getToken();
		long whoId = reader.getWhoId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}
		System.out.println("Getting all texts for whoId: " + whoId);
		Collection<SharedText> allTexts = model.getAllTexts(whoId);
		Collection<SharedTextReply> allTextsReply = adaptTexts(allTexts, whoId);
		writer.createAllTexts(allTextsReply);
	}

	private void processGetText(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing GET_TEXT");
		String token = reader.getToken();
		long textId = reader.getTextId();
		long whoId = reader.getWhoId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Participant participant = model.getParticipants().getParticipant(textId, whoId);
		if (participant == null){
			writer.createKO();
			System.out.println("Participant is null");
			return;
		}

		if (! participant.getRole().canRead()) {
			writer.createKO();
			System.out.println("Participant cannot read");
			return;
		}

		System.out.println("Getting text with textId: " + textId);
		SharedText text = model.getTexts().getSharedText(textId);
		SharedTextReply textReply = adaptText(text, whoId, false);
		writer.createText(textReply);
	}

	private void processNewText(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing NEW_TEXT");
		String token = reader.getToken();
		long creatorId = reader.getWhoId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		User user = model.getUsers().getUser(creatorId);
		if (user == null){
			writer.createKO();
			System.out.println("User is null");
			return;
		}

		if (!model.addText(creatorId)){
			writer.createKO();
			System.out.println("Couldn't create new text");
			return;
		}

		System.out.println("Created new text");
		writer.createOK();
	}

	private void processSaveTextContent(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing SAVE_TEXT_CONTENT");
		String token = reader.getToken();
		long whoId = reader.getWhoId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		long textId = reader.getTextId();

		Participant participant = model.getParticipants().getParticipant(textId, whoId);
		if (participant == null){
			writer.createKO();
			System.out.println("Participant is null");
			return;
		}

		if (! participant.getRole().canEdit()) {
			writer.createKO();
			System.out.println("Participant cannot edit");
			return;
		}

		SharedText text = model.getTexts().getSharedText(textId);
		text.setContent(reader.getContent());
		writer.createOK();
	}

	private void processDeleteText(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing DELETE_TEXT");
		String token = reader.getToken();
		long whoId = reader.getWhoId();
		long textId = reader.getTextId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Participant participant = model.getParticipants().getParticipant(textId, whoId);
		if (participant == null){
			writer.createKO();
			System.out.println("Participant is null");
			return;
		}

		if (! participant.getRole().isCreator()) {
			writer.createKO();
			System.out.println("Participant is not creator");
			return;
		}

		if (!model.removeText(textId)){
			writer.createKO();
			System.out.println("Couldn't delete text");
			return;
		}

		System.out.println("Deleted text");
		writer.createOK();
	}

	private void processGetAllTextOperations(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing GET_ALL_OPERATIONS");
		String token = reader.getToken();
		long whoId = reader.getWhoId();
		long textId = reader.getTextId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Participant participant = model.getParticipants().getParticipant(textId, whoId);
		if (participant == null){
			writer.createKO();
			System.out.println("Participant is null");
			return;
		}

		if (! participant.getRole().canRead()) {
			writer.createKO();
			System.out.println("Participant cannot read");
			return;
		}

		System.out.println("Getting all text operations for textId: " + textId);
		Collection<Operation> allOperations = model.getOperations().getAllOperations(textId);
		Collection<OperationReply> allOperationsReply = adaptOperations(allOperations);

		writer.createAllTextOperations(allOperationsReply);
	}

	private void processGetAllTextParticipants(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing GET_ALL_TEXT_PARTICIPANTS");

		String token = reader.getToken();
		long textId = reader.getTextId();
		long whoId = reader.getWhoId();

		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Participant participant = model.getParticipants().getParticipant(textId, whoId);
		if (participant == null){
			writer.createKO();
			System.out.println("Participant is null");
			return;
		}

		if (! participant.getRole().isCreator()) {
			writer.createKO();
			System.out.println("Participant is not creator");
			return;
		}

		Collection<Participant> allParticipants = model.getParticipants().getAllParticipants(textId);
		Collection<ParticipantReply> allParticipantsReply = adaptParticipants(allParticipants);

		writer.createAllTextParticipants(allParticipantsReply);
	}

	private void processPutAllTextParticipants(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing PUT_ALL_TEXT_PARTICIPANTS");

		String token = reader.getToken();
		long textId = reader.getTextId();
		long whoId = reader.getWhoId();

		/*if (!validToken(token, whoId)) {
			writer.createKO();
			System.out.println("Invalid token");
			return;
		}*/
		if (!model.getTokens().isKnown(token)) {
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Participant submitter = model.getParticipants().getParticipant(textId, whoId);
		if (submitter == null){
			writer.createKO();
			System.out.println("Submitter is null");
			return;
		}

		if (!submitter.getRole().isCreator()) {
			writer.createKO();
			System.out.println("Submitter is not creator");
			return;
		}

		Collection<Participant> toSaveParticipants = reader.getParticipants();
		model.getParticipants().update(textId, toSaveParticipants);
		writer.createOK();
	}

	private void processGetAllUsers(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing GET_ALL_USERS");
		if (!model.getTokens().isKnown(reader.getToken())){
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Collection<User> users = model.getUsers().getAll();
		writer.createAllUsers(users);
	}

	private void processPutAllTextOperations(TCPReader reader, TCPWriter writer) {
		System.out.println("Processing PUT_ALL_TEXT_OPERATIONS");

		if (!model.getTokens().isKnown(reader.getToken())){
			writer.createKO();
			System.out.println("Unknown token");
			return;
		}

		Participant submitter = model.getParticipants().getParticipant(reader.getTextId(), reader.getWhoId());
		if (submitter == null){
			writer.createKO();
			System.out.println("Submitter is null");
			return;
		}

		Collection<Operation> toSave = reader.getOperations();
		for (Operation op : toSave){
			boolean valid = false;
			if (op.getWhat() == What.COMMENT && submitter.getRole().canComment()) valid = true;
			if (op.getWhat() == What.INSERT && submitter.getRole().canEdit()) valid = true;
			if (op.getWhat() == What.DELETE && submitter.getRole().canEdit()) valid = true;
			if (!valid) continue;
			model.getOperations().addOperation(op);
		}

		writer.createOK();

	}

}
