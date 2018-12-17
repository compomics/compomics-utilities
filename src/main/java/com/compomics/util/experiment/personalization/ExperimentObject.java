package com.compomics.util.experiment.personalization;

import com.compomics.util.db.object.DbObject;
import java.util.HashMap;

/**
 * This abstract class provides customization facilities. Tool dependent
 * parameters can be added to classes extending this class.
 *
 * @author Marc Vaudel
 */
public abstract class ExperimentObject extends DbObject {

    /**
     * Empty default constructor
     */
    public ExperimentObject() {
    }

    /**
     * The hash values list.
     */
    private final static long[] HASHVALUELIST = {
        -4785187655948605440L, 2351437082613919744L, -7620983146772158464L, -8747902906952306688L,
        -4496485731609829376L, -4047692981156677632L, 5720028397227347968L, -4748060763626276864L,
        2063365725770047488L, -5756543758428897280L, -134698081630425088L, 867726525032218624L,
        -526450428666544128L, 4146020926348189696L, 4362296343029680128L, -672990070253072384L,
        -2559490283472277504L, 3187632952876974080L, -5716989432807307264L, -8332013824838645760L,
        4253744629365506048L, 2097316067254513664L, 8951627463544416256L, -5600031980443258880L,
        6380991404691560448L, 8903284868402118656L, -1115601857539225600L, 4631654322507227136L,
        7771989044436795392L, 7773688932940122112L, -6019734220953055232L, 3392712990065328128L,
        -8921384047543447552L, -7767842613008707584L, -1186522791511611392L, -6926112736333537280L,
        8736653739320072192L, 8867062073843642368L, 6298992568012455936L, -6831107491093487616L,
        -7084666134747267072L, -1674183307215181824L, 7180054879733344256L, -1774408477150697472L,
        -1102347028329271296L, 2837232313405440000L, 6789844965029836800L, -2021979153929187328L,
        -803643088872329216L, -6635474898121947136L, -1519775710292529152L, -7017431056149018624L,
        8399941098113230848L, 6620078501932513280L, 8402686423795523584L, 7887825026517880832L,
        6240511391300272128L, -2116326148598433792L, 3164957088731514880L, 6354445331039899648L,
        -2421944411545827328L, -6588274517877174272L, -5482092713179058176L, 1515440486213902336L,
        -3383185261582667776L, -2725557693718710272L, 2180993613288613888L, -4878984385226620928L,
        4905597879284899840L, -8937278576235966464L, -4857623260077275136L, -6678664042745585664L,
        6590419491356596224L, 3898378085667969024L, -8773012746479065088L, -4316629602317574144L,
        -578020323565103104L, 5815789437630859264L, 1330829571824838656L, 2058704620696928256L,
        5775301559630338048L, -4128281782811285504L, -6189976155577464832L, -2204893487149668352L,
        -4107985148748068864L, -2803177563490273280L, 7139083951461890048L, -6547891565468342272L,
        3512976861638146048L, 8446989268574042112L, -6262309160844883968L, -447362214463838208L,
        -4695191602764636160L, -8777129286526107648L, -2322220230279856128L, -3376371221541236736L,
        -352816524822126592L, -6489602716775188480L, -4340386299073419264L, -411238008103813120L,
        -7548606038504292352L, 3950672770391547904L, 1570846774247147520L, 2087897268844892160L,
        -6691005528687374336L, 1651506531346769920L, -9105826395118237696L, -920643688498837504L,
        6741095098680469504L, -9196666188088858624L, 4628592761082419200L, 1805632260469598208L,
        -2595685425333377024L, -2001876750192766976L, 4498796613205751808L, -3322677024598908928L,
        8658129466298726400L, 2854559136171276288L, 106897466552897536L, 5590481524594866176L,
        -4319460978758043648L, 1896969526688425984L, -2860223852340688896L, -2273634011107659776L,
        -6830438329227218944L, -1024496407927033856L, -1561168395559655424L, -1430574524350681088L};
    /**
     * Value for a key not set.
     */
    public static final long NO_KEY = asLong("#!#_NO_KEY_#!#");
    /**
     * Map containing user refinement parameters.
     */
    private HashMap<Long, UrParameter> urParams = null;

    /**
     * Adds a user refinement parameter.
     *
     * @param parameter the parameter
     */
    public void addUrParam(UrParameter parameter) {
        writeDBMode();
        
        if (urParams == null) {
            
            createParamsMap();
            
        }
        
        urParams.put(parameter.getParameterKey(), parameter);
        
    }
    
    /**
     * Removes a user parameter from the user parameters map.
     * 
     * @param paramterKey the key of the parameter
     */
    public void removeUrParam(long paramterKey) {
        writeDBMode();
        
        if (urParams != null) {
            
            urParams.remove(paramterKey);
            
        }
    }
    
    /**
     * Creates the parameters map unless done by another thread already.
     */
    private synchronized void createParamsMap() {
        writeDBMode();
        
        if (urParams == null) {
            
            urParams = new HashMap<>(1);
            
        }
    }

    /**
     * Returns the refinement parameter of the same type than the one provided. Null if not found.
     *
     * @param parameter the desired parameter
     * 
     * @return the value stored. Null if not found.
     */
    public UrParameter getUrParam(UrParameter parameter) {
        readDBMode();
        
        if (urParams == null) {
            
            return null;
            
        }
        
        return urParams.get(parameter.getParameterKey());
    }
    
    /**
     * Clears the loaded parameters.
     */
    public void clearParametersMap() {
        writeDBMode();
        
        urParams = null;
    }
    
    /**
     * Sets the user parameters map.
     * 
     * @param urParams the user parameters map
     */
    public void setUrParams(HashMap<Long, UrParameter> urParams){
        writeDBMode();
        
        this.urParams = urParams;
    }
    
    /**
     * Returns the user parameters map.
     * 
     * @return the user parameters map
     */
    public HashMap<Long, UrParameter> getUrParams(){
        readDBMode();
        
        return urParams;
    }

    /**
     * Creating a unique 64 bit hash key from the original key of arbitrary
     * length. The hashed key allows to search entries in the database or in
     * dictionaries in constant time.
     *
     * @param key the original key
     * 
     * @return the hashed key
     */
    public static long asLong(String key) {
        
        long longKey = 0;
        char[] keyAsArray = key.toCharArray();
        
        for (int i = 0; i < keyAsArray.length; ++i) {
        
            long val = HASHVALUELIST[keyAsArray[i]]; // create a hash val of char
            int sft = ((i * 11) & 63); // determine a shift length of cyclic shift depending on char position in key
            val = (val << sft) | (val >>> (64 - sft)); // do the cyclic shift
            longKey ^= val; // xor it with remaining
        
        }
        
        return longKey;
    
    }
}
