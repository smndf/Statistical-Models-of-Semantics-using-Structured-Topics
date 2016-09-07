package classes;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public final class Form {
	public final class InscriptionForm {
		private static final String CHAMP_TEXT  = "text";

		private static final String CHAMP_CONF   = "confirmation";

		private String              resultat;
		private Map<String, String> erreurs      = new HashMap<String, String>();

		public String getResultat() {
			return resultat;
		}

		public Map<String, String> getErreurs() {
			return erreurs;
		}

		/*
		public Entry recordEntry( HttpServletRequest request ) {
			String text = getValeurChamp( request, CHAMP_TEXT );

			Entry entry = new Entry();

			try {
				validationText( text );
			} catch ( Exception e ) {
				setErreur( CHAMP_TEXT, e.getMessage() );
			}
			entry.setText( text );

			if ( erreurs.isEmpty() ) {
				resultat = "Succès de l'inscription.";
			} else {
				resultat = "Échec de l'inscription.";
			}

			return entry;
		}
*/
		private void validationText( String text ) throws Exception {
			if ( text != null ) {
			} else {
				throw new Exception( "Merci de saisir une adresse mail." );
			}
		}

		/*
		 * Ajoute un message correspondant au champ spécifié à la map des erreurs.
		 */
		private void setErreur( String champ, String message ) {
			erreurs.put( champ, message );
		}

		/*
		 * Méthode utilitaire qui retourne null si un champ est vide, et son contenu
		 * sinon.
		 */
		private String getValeurChamp( HttpServletRequest request, String nomChamp ) {
			String valeur = request.getParameter( nomChamp );
			if ( valeur == null || valeur.trim().length() == 0 ) {
				return null;
			} else {
				return valeur.trim();
			}
		}

	}
}
