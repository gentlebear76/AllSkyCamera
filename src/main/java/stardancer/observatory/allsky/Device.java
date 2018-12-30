package stardancer.observatory.allsky;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.indilib.i4j.client.INDIProperty;

public class Device {

    private static final Logger LOGGER = Logger.getLogger(Device.class);

    private String name;
    private LinkedHashMap<String,ArrayList<INDIProperty>> groups;

    public Device(String name){
        this.name=name;
        groups=new LinkedHashMap<>();
    }

    public String getName(){
        return name;
    }

    public void addProperty(INDIProperty p){
        if(p!=null){
            String group_name=p.getGroup();
            if(groups.containsKey(group_name)){
                groups.get(group_name).add(p);
            }else{
                groups.put(group_name, new ArrayList<>());
                groups.get(group_name).add(p);
            }
        }
    }

    public void removeProperty(INDIProperty p){
        if(p!=null){
            String group_name=p.getGroup();
            ArrayList<INDIProperty> list=groups.get(group_name);
            if(list!=null && list.contains(p)){
                list.remove(p);
                if(list.size()==0){
                    groups.remove(group_name);
                }
            }
        }
    }

    public void updateProperty(INDIProperty p){
        if(p!=null){
            String group_name=p.getGroup();
            ArrayList<INDIProperty> list=groups.get(group_name);
            for(int i=0;i<list.size();i++){
                if(list.get(i).getName().equals(p.getName())){
                    list.set(i,p);
                }
            }
        }
    }

    public void clear(){
        groups.clear();
    }

    public ArrayList<INDIProperty> getGroupProperties(String group){
        return groups.get(group);
    }

    public ArrayList<String> getGroupsNames(){
        ArrayList<String> names = new ArrayList<>();
        for (String name : groups.keySet()) {
            names.add(name);
        }

        return names;
    }

    public ArrayList<INDIProperty> getAllProperties(){
        ArrayList<INDIProperty> properties = new ArrayList<>();
        for (String name : groups.keySet()) {
            properties.addAll(groups.get(name));
        }

        return properties;
    }
}