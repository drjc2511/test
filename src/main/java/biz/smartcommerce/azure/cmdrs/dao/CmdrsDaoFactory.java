package biz.smartcommerce.azure.cmdrs.dao;

public class CmdrsDaoFactory {
    private static CmdrsDAO cmdrsDAO;

    public static CmdrsDAO getDao() {
        if (cmdrsDAO == null) {
        	cmdrsDAO = new DocDbDao();
        }
        return cmdrsDAO;
    }
}
