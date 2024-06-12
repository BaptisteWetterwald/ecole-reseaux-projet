package fr.ensisa.hassenforder.tp.client.network;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;

import fr.ensisa.hassenforder.tp.client.model.Credential;
import fr.ensisa.hassenforder.tp.client.model.Participant;
import fr.ensisa.hassenforder.tp.client.model.SharedText;
import fr.ensisa.hassenforder.tp.client.model.TextOperation;
import fr.ensisa.hassenforder.tp.client.model.User;
import fr.ensisa.hassenforder.tp.network.Protocol;

public class NetworkSession implements ISession {

    private Socket tcp;
    private String host;
    private int port;

    public NetworkSession(String host, int port) {
    	this.host = host;
    	this.port = port;
    }

    @Override
    synchronized public boolean close() {
        try {
            if (tcp != null) {
                tcp.close();
            }
            tcp = null;
        } catch (IOException e) {
        }
        return true;
    }

    @Override
    synchronized public boolean open() {
        this.close();
        try {
            tcp = new Socket(this.host, this.port);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public User connect(String mail, String passwd) {
        try {
            TPWriter w = new TPWriter(tcp.getOutputStream());
            w.connect(mail, passwd);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            if (r.getType() == Protocol.REPLY_USER) return r.getUser();

            return null;
        } catch (IOException e) {
            return null;
        }
    }

	@Override
	public Credential getCredential(String token, long id) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.getCredential(token, id);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            if (r.getType() == Protocol.REPLY_CREDENTIAL) return r.getCredential();
    		return null;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean createUser(Credential credential) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.createUser(credential);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for createUser : " + valid.toString());
            return valid;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean updateUser(String token, Credential credential) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.updateUser(token, credential);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for updateUser : " + valid.toString());
            return valid;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Collection<SharedText> getAllTexts(String token, long who) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.getAllTexts(token, who);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            if (r.getType() == Protocol.REPLY_ALL_TEXTS) {
            	Collection<SharedText> allTexts = r.getAllTexts();
            	System.out.println("Got all texts:" + allTexts.toString());
            	return allTexts;
            }
    		return null;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public SharedText getText(String token, long textId, long whoId) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
			w.getText(token, textId, whoId);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            if (r.getType() == Protocol.REPLY_TEXT){
            	SharedText text = r.getText();
            	System.out.println("Received a text");
            	return text;
            }
    		return null;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean saveTextContent(String token, long textId, long whoId, String content) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.saveTextContent(token, textId, whoId, content);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();

            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for saveTextContent : " + valid.toString());
            return valid;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean deleteText(String token, long textId, long whoId) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.deleteText(token, textId, whoId);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();

            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for deleteText : " + valid.toString());
            return valid;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean newText(String token, long creatorId) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.newText(token, creatorId);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();

            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for newText : " + valid.toString());
            return valid;

        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Collection<Participant> getAllTextParticipants(String token, long textId, long whoId) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.getAllTextParticipants(token, textId, whoId);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();

            if (r.getType() == Protocol.REPLY_ALL_PARTICIPANTS) {
            	Collection<Participant> allParticipants= r.getAllTextParticipants();
            	System.out.println("Got all participants:" + allParticipants.toString());
            	return allParticipants;
            }

            return null;

        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Collection<User> getAllUsers(String token) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.getAllUsers(token);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();

            if (r.getType() == Protocol.REPLY_ALL_USERS) {
            	Collection<User> allUsers = r.getAllUsers();
            	System.out.println("Got all users:" + allUsers.toString());
            	return allUsers;
            }

    		return null;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Collection<TextOperation> getAllTextOperations(String token, long textId, long whoId) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.getAllTextOperations(token, textId, whoId);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            if (r.getType() == Protocol.REPLY_ALL_OPERATIONS) {
            	Collection<TextOperation> allOperations = r.getAllTextOperations();
            	System.out.println("Got all text operations:" + allOperations.toString());
            	return allOperations;
            }
    		return null;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean saveTextParticipants(String token, long textId, long whoId, Collection<ParticipantOperationRequest> toSave) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.saveTextParticipants(token, textId, whoId, toSave);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for saveTextParticipants : " + valid.toString());
            return valid;
        } catch (IOException e) {
    		return null;
        }
	}

	@Override
	public Boolean saveTextOperations(String token, long textId, long whoId, Collection<TextOperationRequest> toSave) {
        try {
        	TPWriter w = new TPWriter(tcp.getOutputStream());
        	w.saveTextOperations(token, textId, whoId, toSave);
            w.send();

            TPReader r = new TPReader(tcp.getInputStream());
            r.receive();
            Boolean valid = r.getType() == Protocol.REPLY_OK;
            System.out.println("Received result for saveTextOperations : " + valid.toString());
            return valid;
        } catch (IOException e) {
    		return null;
        }
	}

}
