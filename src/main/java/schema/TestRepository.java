package schema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;


@Component
public class TestRepository {
	
    private final Map< Long , Cow> cows = new HashMap< Long , Cow>();
    private final Map<Long, Herd> herds = new HashMap<Long, Herd>();
    private final Map<Long, Vector<CowBcs>> cowBcss  = new HashMap<Long, Vector<CowBcs>>();
    private final Map<Long, AnimalAlert> animalAlerts = new HashMap<Long, AnimalAlert>();
    private final Map<Long, HerdAlert> herdAlerts = new HashMap<Long, HerdAlert>();
    
    //variable para generar id de rodeo
    private int lastHerdId = 0;
    
    //variable para generar id de vacas
    private int lastCowId = 0;
    
    @PostConstruct
    public void initData() {
    	
    }

	public AnimalInfo getInfoCow(long id) {
	
		AnimalInfo n = null;
		Cow c = cows.get(id);
		
		if(c != null) {
			n = new AnimalInfo();
			n.setCantidadPartos(c.getCantidadPartos());
			n.setElectronicId(c.getElectronicId());
			n.setFechaNacimiento(c.getFechaNacimiento());
			n.setId(id);
			n.setPeso(c.getPeso());
			n.setUltimaFechaParto(c.getUltimaFechaParto());
			n.setHerdId(c.getHerdId());
			
			n.setCc(0);
			n.setCowBcsId(-1);;
			
			if(cowBcss.get(id) != null) {
				CowBcs cb = cowBcss.get(id).lastElement();
				n.setCc(cb.getCc());
				n.setCowBcsId(new Long(cb.getId()));
				n.setFechaBcs(cb.getFecha());
			}
		}
		return n;
	}

	public Long addHerd(AddHerdRequest request) {
		
		Herd herd = new Herd();
    	herd.setId(lastHerdId);
    	herd.setLocation(request.getLocation());
		herds.put(herd.getId(), herd);
		lastHerdId++;
		return herd.getId();
	}
	
	public int addCow(CreateCowRequest c) {
		
		//control de herd
		if(!herds.containsKey(c.getHerdId())) {
			return -1;
		}
		
		//control de peso
		if(c.getPeso()<=0f)
			return -1;
		
		//control fecha de parto < fecha de nacimiento
		if(c.getUltimaFechaParto() != null && c.getUltimaFechaParto().compare(c.getFechaNacimiento())!= 1) {
			return -1;
		}
		
		//se crea crea la vaca
		Cow cow = new Cow();
		cow.setCantidadPartos( (int) c.getCantidadPartos());
		cow.setElectronicId(c.getElectronicId());
		cow.setFechaNacimiento(c.getFechaNacimiento());
		cow.setPeso(c.getPeso());
		cow.setUltimaFechaParto(c.getUltimaFechaParto());
		cow.setHerdId((int) c.getHerdId());
		cow.setId(lastCowId);
		
		//actualizar herd
		herds.get(cow.getHerdId()).getCows().add(cow);
			
		//agregar cow
		cows.put((long)lastCowId,cow);
		lastCowId++;
		
		return lastCowId-1;
	}	

	//IA que ingresa bcs
	public boolean addCowBcs(AddBcsRequest request) {
		//si existe el id de vaca y la fecha de bcs es mayor al nacimiento
		if(cows.containsKey(request.getCowId()) && cows.get(request.getCowId()).getFechaNacimiento().compare(request.getFecha()) != -1)
			return false;
		
		//condicion corporal >=1 ,<=9
		if(request.getCc() <1 || request.getCc() >9)
			return false;
		
		//comprobar que la fecha sea mayor al bcs anterior, si lo hay .
		if (cowBcss.containsKey(request.getCowId())) {
			if (cowBcss.get(request.getCowId()).lastElement().getFecha().compare(request.getFecha()) != -1 )
				return false;
		}
		
		//comprobar si hay alerta de vaca para esa condicion
		if ((animalAlerts.containsKey(request.getCowId())&& animalAlerts.get(request.getCowId()).getBcsThresholdMin() >= request.getCc()) ||
				(animalAlerts.containsKey(request.getCowId())&& animalAlerts.get(request.getCowId()).getBcsThresholdMax() <= request.getCc()))
			AlertaCow();		
		
		CowBcs cb = new CowBcs();
		cb.setCowId(request.getCowId());
		cb.setFecha(request.getFecha());
		cb.setCc(request.getCc());
		
		//si ya tiene cargados otros bcs
		if (cowBcss.containsKey(cb.getCowId()) ) {
			cb.setId(cowBcss.get(cb.getCowId()).size());
			cowBcss.get(cb.getCowId()).add(cb);
		}
		else {
			Vector<CowBcs> v = new Vector<CowBcs>();
			cb.setId(0);
			v.add(cb);
			cowBcss.put(cb.getCowId(), v);
		}
		
		long index = getInfoCow(request.getCowId()).getHerdId();
		
		//comprobar si hay alerta de rodeo
		if (herdAlerts.containsKey(index)) {
			float max = herdAlerts.get(index).getBcsThresholdMax();
			float min = herdAlerts.get(index).getBcsThresholdMin();
			float prom = calcularBcsProm(index, null);
			if(min >= prom || max <= prom)
				AlertaHerd();
		}
		return true;
	}

	//agregar alerta a vaca
	public boolean setAnimalAlert(SetAnimalAlertRequest request) {
		//control de que exista el animal
		if (!cows.containsKey(request.getAnimalId()))
			return false;
		
		AnimalAlert a = new AnimalAlert();
		a.setAnimalId(request.getAnimalId());
		a.setBcsThresholdMax(request.getBcsThresholdMax());
		a.setBcsThresholdMin(request.getBcsThresholdMin());
		
		 animalAlerts.put(a.getAnimalId(), a);
		 return true;
	}

	//agregar limite de alerta a rodeo
	public boolean setHerdAlert(SetHerdAlertRequest request) {
		//control de que exista el herd
		if (!herds.containsKey(request.getHerdId()))
			return false;
		
		HerdAlert a = new HerdAlert();
		a.setHerdId(request.getHerdId());
		a.setBcsThresholdMax(request.getBcsThresholdMax());
		a.setBcsThresholdMin(request.getBcsThresholdMin());
		
		 herdAlerts.put(request.getHerdId(), a);
		 return true;
	}
	
	private void AlertaCow() {
		//NO HAY COMPORTAMIENTO DEFINIDO PARA ESTA FUNCION
		System.out.print("ALERTA! EL BCS DE LA VACA ESTA AL LIMITE\n");
	}
	
	private void AlertaHerd() {
		//NO HAY COMPORTAMIENTO DEFINIDO PARA ESTA FUNCION
		System.out.print("ALERTA! EL BCS DEl RODEO ESTA AL LIMITE\n");
	}

	//obtener toda la informacion del rodeo
	public HerdInfo getInfoRodeo(GetInfoRodeoRequest request) {
		
		//comprobar si existe el herd
		if (!herds.containsKey(request.getId()))
			return null;
		
		HerdInfo h = new HerdInfo();
		h.setLocation(herds.get(request.getId()).getLocation());
		ArrayList<AnimalInfo> ai = new ArrayList<AnimalInfo>();

		h.setBcsPromedio(calcularBcsProm(request.getId(), ai));
		h.getCows().addAll(ai);

		return h;
	}
	
	private float calcularBcsProm(Long id, ArrayList<AnimalInfo> ai ) {
		float prom = 0f;
		int cantBcs = 0;
		
		int size = herds.get(id).getCows().size();
		for( int i=0; i< size ; i++) {
			Long index = herds.get(id).getCows().get(i).getId();
			if (ai!= null)
				ai.add(getInfoCow(index));
			
			if (cowBcss.containsKey(index)) {
				float cc = cowBcss.get(index).lastElement().getCc();
				prom +=  cc;
				cantBcs++;
			}
		}
		
		if(cantBcs != 0)
			prom = prom/ cantBcs ;
		
		return prom;
	}
}
