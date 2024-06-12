package fr.ensisa.hassenforder.tp.client.network;

import java.util.Collection;

import fr.ensisa.hassenforder.tp.client.model.Credential;
import fr.ensisa.hassenforder.tp.client.model.Participant;
import fr.ensisa.hassenforder.tp.client.model.SharedText;
import fr.ensisa.hassenforder.tp.client.model.TextOperation;
import fr.ensisa.hassenforder.tp.client.model.User;

/**
 *
 * @author hassenforder
 */
public interface ISession {

    boolean open ();
    boolean close ();

    User connect(String mail, String passwd); // done
	Boolean createUser(Credential credential); // done
	Credential getCredential(String token, long whoId); // done
	Boolean updateUser(String token, Credential credential); // done

	Collection<SharedText> getAllTexts(String token, long whoId); // done
	SharedText getText(String token, long textId, long whoId); // done
	Boolean saveTextContent(String token, long textId, long whoId, String content); // done
	Boolean deleteText(String token, long textId, long whoId); // done
	Boolean newText(String token, long whoId); // done

	Collection<Participant> getAllTextParticipants(String token, long textId, long whoId); // done
	Boolean saveTextParticipants(String token, long textId, long whoId, Collection<ParticipantOperationRequest> toSave); // done
	Collection<User> getAllUsers(String token); // done

	Collection<TextOperation> getAllTextOperations(String token, long textId, long whoId); // done
	Boolean saveTextOperations(String token, long textId, long whoId, Collection<TextOperationRequest> toSave); // done

}
