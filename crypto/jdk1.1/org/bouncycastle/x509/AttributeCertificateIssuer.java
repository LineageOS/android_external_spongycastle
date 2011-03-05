package org.spongycastle.x509;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.x509.AttCertIssuer;
import org.spongycastle.asn1.x509.GeneralName;
import org.spongycastle.asn1.x509.GeneralNames;
import org.spongycastle.asn1.x509.V2Form;
import org.spongycastle.asn1.x509.X509Name;
import org.spongycastle.jce.PrincipalUtil;
import org.spongycastle.jce.X509Principal;

/**
 * Carrying class for an attribute certificate issuer.
 */
public class AttributeCertificateIssuer
    implements CertSelector
{
    ASN1Encodable  form;
    
    /**
     * @param issuer
     */
    AttributeCertificateIssuer(
        AttCertIssuer issuer)
    {
        form = issuer.getIssuer();
    }
    
    public AttributeCertificateIssuer(
        X509Principal principal) 
    {        
        form = new V2Form(new GeneralNames(new DERSequence(new GeneralName(principal))));
    }
    
    private Object[] getNames()
    {
        GeneralNames    name;
        
        if (form instanceof V2Form)
        {
            name = ((V2Form)form).getIssuerName();
        }
        else
        {
            name = (GeneralNames)form;
        }
        
        GeneralName[]   names = name.getNames();
        
        ArrayList   l = new ArrayList(names.length);
        
        for (int i = 0; i != names.length; i++)
        {
            if (names[i].getName() instanceof X509Name)
            {
                try
                {
                    l.add(new X509Principal(((X509Name)names[i].getName()).getEncoded()));
                }
                catch (IOException e)
                {
                    throw new RuntimeException("badly formed Name object");
                }
            }
        }
        
        return l.toArray(new Object[l.size()]);
    }
    
    /**
     * Return any principal objects inside the attribue certificate issuer object.
     * 
     * @return an array of Principal objects (usually X500Principal)
     */
    public Principal[] getPrincipals()
    {
        Object[]    p = this.getNames();
        ArrayList   l = new ArrayList();
        
        for (int i = 0; i != p.length; i++)
        {
            if (p[i] instanceof Principal)
            {
                l.add(p);
            }
        }
        
        return (Principal[])l.toArray(new Principal[l.size()]);
    }
    
    private boolean matchesDN(X509Principal subject, GeneralNames targets)
    {
        GeneralName[]   names = targets.getNames();

        for (int i = 0; i != names.length; i++)
        {
            GeneralName gn = names[i];

            if (gn.getTagNo() == 4)
            {
                try
                {
                    if (new X509Principal(((ASN1Encodable)gn.getName()).getEncoded()).equals(subject))
                    {
                        return true;
                    }
                }
                catch (IOException e)
                {
                }
            }
        }

        return false;
    }
    
    /* (non-Javadoc)
     * @see java.security.cert.CertSelector#clone()
     */
    public Object clone()
    {
        return new AttributeCertificateIssuer(AttCertIssuer.getInstance(form));
    }
    
    /* (non-Javadoc)
     * @see java.security.cert.CertSelector#match(java.security.cert.Certificate)
     */
    public boolean match(Certificate cert)
    {
        if (!(cert instanceof X509Certificate))
        {
            return false;
        }
        
        X509Certificate x509Cert = (X509Certificate)cert;
        
        try
        {
            if (form instanceof V2Form)
            {
                V2Form issuer = (V2Form)form;
                if (issuer.getBaseCertificateID() != null)
                {
                    if (issuer.getBaseCertificateID().getSerial().getValue().equals(x509Cert.getSerialNumber())
                        && matchesDN(PrincipalUtil.getIssuerX509Principal(x509Cert), issuer.getBaseCertificateID().getIssuer()))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                
                GeneralNames name = issuer.getIssuerName();
                if (matchesDN(PrincipalUtil.getSubjectX509Principal(x509Cert), name))
                {
                    return true;
                }
            }
            else
            {
                GeneralNames name = (GeneralNames)form;
                if (matchesDN(PrincipalUtil.getSubjectX509Principal(x509Cert), name))
                {
                    return true;
                }
            }
        }
        catch (CertificateEncodingException e)
        {
            return false;
        }

        return false;
    }
}
