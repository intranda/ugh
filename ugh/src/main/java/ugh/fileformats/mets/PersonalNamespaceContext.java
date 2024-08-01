package ugh.fileformats.mets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/*******************************************************************************
 * <p>
 * PersonalNamespaceContext.
 * </p>
 ******************************************************************************/

public class PersonalNamespaceContext implements NamespaceContext {

    // Key is the prefix; value is the URI (as a String).
    private Map<String, Namespace> namespaceHash = null;

    /***************************************************************************
     * @return the namespaceHash
     **************************************************************************/
    public Map<String, Namespace> getNamespaceHash() {
        return this.namespaceHash;
    }

    /***************************************************************************
     * @param namespaceHash the namespaceHash to set
     **************************************************************************/
    public void setNamespaceHash(Map<String, Namespace> namespaceHash) {
        this.namespaceHash = namespaceHash;
    }

    /*
     * (non-Javadoc)
     * 
     * This method is only be called, if there is a prefix to an element.
     * 
     * @see
     * javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    @Override
    public String getNamespaceURI(String prefix) {

        String uri = null;

        if (prefix == null) {
            throw new NullPointerException("No prefix given; prefix is null!");
        }

        if ("".equals(prefix)) {
            // We are asking for the default namespace.
            Namespace ns = this.getDefaultNamespace();
            return ns.getUri();
        }

        Namespace ns = this.getNamespaceHash().get(prefix);
        if (ns != null) {
            uri = ns.getUri();
        }
        if (uri != null) {
            return uri;
        }

        return XMLConstants.DEFAULT_NS_PREFIX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    @Override
    public String getPrefix(String uri) {

        if (uri == null) {
            return null;
        }

        Map<String, Namespace> hm = this.getNamespaceHash();
        Set<String> keyset = hm.keySet();

        for (String key : keyset) {
            // Get the uri for the key.
            Namespace keysns = hm.get(key);
            String keysuri = keysns.getUri();
            if (uri.equals(keysuri)) {
                // This is the right uri, so key is the prefix we are
                // looking for.
                return key;
            }
        }

        // No uri was found.
        return null;
    }

    /***************************************************************************
     * @return
     **************************************************************************/
    public Namespace getDefaultNamespace() {

        Map<String, Namespace> hm = this.getNamespaceHash();
        Set<String> keyset = hm.keySet();

        for (String key : keyset) {
            // Get the uri for the key.
            Namespace keysns = hm.get(key);
            // Is this the default namespace?
            if (keysns.getDefaultNS().booleanValue()) {
                return keysns;
            }
        }

        // No uri was found.
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    @Override
    public Iterator<String> getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}
