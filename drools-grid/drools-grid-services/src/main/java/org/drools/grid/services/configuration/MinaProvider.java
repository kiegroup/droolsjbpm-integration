package org.drools.grid.services.configuration;




/**
 * @author salaboy
 */
public class MinaProvider extends GenericProvider {
    
    private String providerAddress;
    private int providerPort;

    public MinaProvider() {	}
    
    public MinaProvider(String providerAddress, int providerPort) {
        this.providerAddress = providerAddress;
        this.providerPort = providerPort;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public int getProviderPort() {
        return providerPort;
    }

	public void setProviderAddress(String providerAddress) {
		this.providerAddress = providerAddress;
	}

	public void setProviderPort(int providerPort) {
		this.providerPort = providerPort;
	}

	@Override
	public String getId() {
		return "MinaProvider:"+providerAddress+":"+providerPort;
	}
	
	@Override
	public ProviderType getProviderType() {
		return ProviderType.RemoteMina;
	}
}
