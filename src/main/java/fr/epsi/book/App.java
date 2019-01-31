package fr.epsi.book;

import fr.epsi.book.dal.BookDAO;
import fr.epsi.book.dal.ContactDAO;
import fr.epsi.book.domain.Book;
import fr.epsi.book.domain.Contact;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class App {

    private static final String BOOK_EXPORT_CSV_DIR = "./resources/export/csv/";
    private static final String BOOK_EXPORT_XML_DIR = "./resources/export/xml/";

    private static final Scanner sc = new Scanner( System.in );
    private static Book book;
    private static BookDAO bookDAO = new BookDAO();
    private static ContactDAO contactDAO = new ContactDAO();

    public static void main( String... args ) {
        try {
            dspBookMenu();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Contact.Type getTypeFromKeyboard() {
        int response;
        boolean first = true;
        do {
            if ( !first ) {
                System.out.println( "************************************************" );
                System.out.println( "* Mauvais choix, merci de recommencer !        *" );
                System.out.println( "************************************************" );
            }
            System.out.println( "*******Choix type de contact ********" );
            System.out.println( "* 1 - Perso                         *" );
            System.out.println( "* 2 - Pro                          *" );
            System.out.println( "*************************************" );
            System.out.print( "*Votre choix : " );
            try {
                response = sc.nextInt() - 1;
            } catch ( InputMismatchException e ) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while ( 0 != response && 1 != response );
        return Contact.Type.values()[response];
    }

    private static void addContact() throws SQLException {
        System.out.println( "***************************************" );
        System.out.println( "**********Ajout d'un contact***********" );
        Contact contact = new Contact();
        System.out.print( "Entrer le nom :" );
        contact.setName( sc.nextLine() );
        System.out.print( "Entrer l'email :" );
        contact.setEmail( sc.nextLine() );
        System.out.print( "Entrer le téléphone :" );
        contact.setPhone( sc.nextLine() );
        contact.setType( getTypeFromKeyboard() );
        contact.setBook(book);
        book.addContact( contact );
        contactDAO.create(contact);
        System.out.println( "Nouveau contact ajouté ..." );
    }

    private static void editContact() throws SQLException {
        System.out.println( "**********************************************" );
        System.out.println( "***********Modification d'un contact**********" );
        dspContacts( false );
        System.out.print( "Entrer l'identifiant du contact : " );
        Long id = sc.nextLong();
        sc.nextLine();
        Contact contact = book.getContacts().get( id );
        if ( null == contact ) {
            System.out.println( "Aucun contact trouvé avec cet identifiant ..." );
        } else {
            System.out
                .print( "Entrer le nom ('" + contact.getName() + "'; laisser vide pour ne pas mettre à jour) : " );
            String name = sc.nextLine();
            if ( !name.isEmpty() ) {
                contact.setName( name );
            }
            System.out.print( "Entrer l'email ('" + contact
                .getEmail() + "'; laisser vide pour ne pas mettre à jour) : " );
            String email = sc.nextLine();
            if ( !email.isEmpty() ) {
                contact.setEmail( email );
            }
            System.out.print( "Entrer le téléphone ('" + contact
                .getPhone() + "'; laisser vide pour ne pas mettre à jour) : " );
            String phone = sc.nextLine();
            if ( !phone.isEmpty() ) {
                contact.setPhone( phone );
            }
            contactDAO.update(contact);
            System.out.println( "Le contact a bien été modifié ..." );
        }
    }

    private static void deleteContact() throws SQLException {
        System.out.println( "**********************************************" );
        System.out.println( "***********Suppression d'un contact***********" );
        dspContacts( false );
        System.out.print( "Entrer l'identifiant du contact : " );
        Long id = sc.nextLong();
        sc.nextLine();
        Contact contact = book.getContacts().remove( id );
        if ( null == contact ) {
            System.out.println( "Aucun contact trouvé avec cet identifiant ..." );
        } else {
            contactDAO.remove(contact);
            System.out.println( "Le contact a bien été supprimé ..." );
        }
    }

    private static void sort() {
        int response;
        boolean first = true;
        do {
            if ( !first ) {
                System.out.println( "************************************************" );
                System.out.println( "* Mauvais choix, merci de recommencer !        *" );
                System.out.println( "************************************************" );
            }
            System.out.println( "*******Choix du critère*******" );
            System.out.println( "* 1 - Nom     **              *" );
            System.out.println( "* 2 - Email **                *" );
            System.out.println( "*******************************" );
            System.out.print( "*Votre choix : " );
            try {
                response = sc.nextInt();
            } catch ( InputMismatchException e ) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while ( 0 >= response || response > 2 );
        Map<Long, Contact> contacts = book.getContacts();
        switch ( response ) {
            case 1:
                contacts.entrySet().stream()
                    .sorted( ( e1, e2 ) -> e1.getValue().getName().compareToIgnoreCase( e2.getValue().getName() ) )
                    .forEach( e -> dspContact( e.getValue() ) );
                break;
            case 2:

                contacts.entrySet().stream().sorted( ( e1, e2 ) -> e1.getValue().getEmail()
                    .compareToIgnoreCase( e2.getValue().getEmail() ) )
                    .forEach( e -> dspContact( e.getValue() ) );
                break;
        }
    }

    private static void searchContactsByName() {

        System.out.println( "********************************************************************" );
        System.out.println( "*************Recherche de contacts sur le nom ou l'email************" );
        System.out.println( "********************************************************************" );
        System.out.print( "*Mot clé (1 seul) : " );
        String word = sc.nextLine();
        Map<String, Contact> subSet = book.getContacts().entrySet().stream()
            .filter( entry -> entry.getValue().getName().contains( word ) || entry
                .getValue().getEmail().contains( word ) )
            .collect( HashMap::new, ( newMap, entry ) -> newMap
                .put( entry.getKey().toString(), entry.getValue() ), Map::putAll );

        if ( subSet.size() > 0 ) {
            System.out.println( subSet.size() + " contact(s) trouvé(s) : " );
            subSet.forEach((key, value) -> dspContact(value));
        } else {
            System.out.println( "Aucun contact trouvé avec cet identifiant ..." );
        }
    }

    private static void dspContact(Contact contact) {
        System.out.println( contact.getId() + "\t\t\t\t" + contact.getName() + "\t\t\t\t" + contact
            .getEmail() + "\t\t\t\t" + contact.getPhone() + "\t\t\t\t" + contact.getType() );
    }

    private static void dspContacts(boolean dspHeader) {
        if ( dspHeader ) {
            System.out.println( "***************************************" );
            System.out.println( "*********Liste de vos contacts*********" );
        }
        for ( Map.Entry<Long, Contact> entry : book.getContacts().entrySet() ) {
            dspContact( entry.getValue() );
        }
        System.out.println( "***************************************" );
    }

    private static  void dspBookMenu() throws SQLException {
        int response;
        boolean first = true;
        do {
            System.out.println("***************************************");
            System.out.println("*****************Menu******************");
            System.out.println("* 1 - Ajouter un carnet d'adresse     *");
            System.out.println("* 2 - Modifier un carnet d'adresse    *");
            System.out.println("* 3 - Gérer un carnet d'adresse       *");
            System.out.println("* 4 - Quitter                         *");
            System.out.println("***************************************");
            System.out.print("*Votre choix : ");
            try {
                response = sc.nextInt();
            } catch (InputMismatchException e) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while (1 > response || 4 < response);
        switch (response) {
            case 1:
                addBook();
                dspMainMenu();
                break;
            case 2:
                editBook();
                dspBookMenu();
                break;
            case 3:
                displayBook();
                dspMainMenu();
                break;
        }
    }

    private static void displayBook() throws SQLException {
        System.out.println( "***************************************" );
        System.out.println( "*********Affichage d'un carnet*********" );
        Long id;
        do {
            List<Book> list = bookDAO.findAll();
            System.out.println();
            for(Book b : list){
                System.out.println("ID du carnet : "+b.getId()+". Code du carnet : "+b.getCode());
            }
            System.out.println();
            System.out.print("\nEntrer l'identifiant du carnet que vous voulez afficher : ");
            id = sc.nextLong();
            sc.nextLine();
            book = bookDAO.findById(id);
        }while(book == null);
        ArrayList<Contact> listContact = (ArrayList<Contact>) contactDAO.findByBookId(id);
        for (Contact contact : listContact){
            book.addContact(contact);
        }
    }

    private static void editBook() throws SQLException {
        System.out.println( "**********************************************" );
        System.out.println( "***********Modification d'un carnet***********" );
        Long id;
        do {
            List<Book> list = bookDAO.findAll();
            System.out.println();
            for(Book b : list){
                System.out.println("ID du carnet : "+b.getId()+". Code du carnet : "+b.getCode());
            }
            System.out.println();
            System.out.print("\nEntrer l'identifiant du carnet que vous voulez afficher : ");
            id = sc.nextLong();
            sc.nextLine();
            book = bookDAO.findById(id);
        }while(book == null);
        Book book = bookDAO.findById(id);
        if ( null == book ) {
            System.out.println( "Aucun contact trouvé avec cet identifiant ..." );
        } else {
            System.out.println("Veuillez saisir le nouveau code de ce carnet : ");
            String code = sc.nextLine();
            if ( !code.isEmpty() ) {
                book.setCode(code);
            }
            bookDAO.update(book);
        }
    }



    private static void addBook() throws SQLException {
        System.out.println( "***************************************" );
        System.out.println( "**********Ajout d'un carnet************" );
        book = new Book();
        System.out.println("Entrez le code :");
        book.setCode(sc.nextLine());
        bookDAO.create(book);
        System.out.println("Nouveau carnet ajouté ...");
    }

    private static void dspMainMenu() throws SQLException {
        int response;
        boolean first = true;
        do {
            if ( !first ) {
                System.out.println( "************************************************" );
                System.out.println( "* Mauvais choix, merci de recommencer !        *" );
                System.out.println( "************************************************" );
            }
            System.out.println( "***************************************" );
            System.out.println( "*****************Menu******************" );
            System.out.println( "* 1 - Ajouter un contact              *" );
            System.out.println( "* 2 - Modifier un contact             *" );
            System.out.println( "* 3 - Supprimer un contact            *" );
            System.out.println( "* 4 - Lister les contacts             *" );
            System.out.println( "* 5 - Rechercher un contact           *" );
            System.out.println( "* 6 - Trier les contacts              *" );
            System.out.println( "* 7 - Export des contacts             *" );
            System.out.println( "* 8 - Retourner aux menus des carnets *" );
            System.out.println( "***************************************" );
            System.out.print( "*Votre choix : " );
            try {
                response = sc.nextInt();
            } catch ( InputMismatchException e ) {
                response = -1;
            } finally {
                sc.nextLine();
            }
            first = false;
        } while ( 1 > response || 8 < response );
        switch ( response ) {
            case 1:
                addContact();
                dspMainMenu();
                break;
            case 2:
                editContact();
                dspMainMenu();
                break;
            case 3:
                deleteContact();
                dspMainMenu();
                break;
            case 4:
                dspContacts( true );
                dspMainMenu();
                break;
            case 5:
                searchContactsByName();
                dspMainMenu();
                break;
            case 6:
                sort();
                dspMainMenu();
                break;
            case 7:
                exportContacts();
                dspMainMenu();
                break;
            case 8:
                dspBookMenu();
                break;
        }
    }

    private static void exportContacts() {
        System.out.println("*Voulez-vous exporter : ");
        System.out.println("* 1 - En XML");
        System.out.println("* 2 - En CSV");
        System.out.print("*Votre choix : ");
        int response = -1;
        try {
            response = sc.nextInt();
        }
        catch ( InputMismatchException ignored) {}
        finally {
            sc.nextLine();
        }
        String exportFileName = new SimpleDateFormat( "yyyy-MM-dd-hh-mm-ss" ).format( new Date() );
        if(response == 1) {
            exportFileName = exportFileName + ".xml";
            try {
                JAXBContext context = JAXBContext.newInstance(Book.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(book, new FileOutputStream(BOOK_EXPORT_XML_DIR + exportFileName));
            } catch (JAXBException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            if (response == 2){
                exportFileName = exportFileName + ".csv";
                try {
                    BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                            new FileOutputStream( BOOK_EXPORT_CSV_DIR + exportFileName), StandardCharsets.UTF_8
                        )
                    );
                    book.getContacts().forEach((key, contact) -> {
                        String oneLine = contact.getId() + ";" + contact.getName() + ";" + contact.getEmail() +
                            ";" + contact.getPhone() + ";" + contact.getType();
                        try {
                            bw.write(oneLine);
                            bw.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    bw.flush();
                    bw.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
