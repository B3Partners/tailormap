/* 
 * Copyright (C) 2012 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @class 
 * @constructor
 * @description Flamingo Image layer class 
 **/

Ext.define("viewer.viewercontroller.flamingo.FlamingoImageLayer",{
    extend: "viewer.viewercontroller.flamingo.FlamingoLayer",
    config:{
        id:null,
        url:null,
        extent:null
    },
    constructor: function(config){
        viewer.viewercontroller.flamingo.FlamingoWMSLayer.superclass.constructor.call(this, config);
        this.initConfig(config);
        this.type=viewer.viewercontroller.controller.Layer.IMAGE_TYPE;
        return this;
    },
    getTagName : function(){
        return "LayerImage";
    },    
    /**
     *makes a xml string so the object can be added to flamingo
     *@return a xml string of this object
     **/
    toXML : function(){
        //<fmc:LayerImage imageurl="../images/limburg2010.swf" extent="168000,307000,214000,421000" listento="map"/>
        var xml="<fmc:";
        xml+=this.getTagName();
        xml+=" xmlns:fmc=\"fmc\"";
        xml+=" id=\""+this.id+"\"";
     
        xml+=" url=\""+this.url+"\"";
        xml+=" extent=\""+this.extent;
       
        xml+="\"";
       /* for (var optKey in this.options){
            //skip these options.
            if (optKey.toLowerCase()== "url" ||
                optKey.toLowerCase()== "sld"){}
            else{
                xml+=" "+optKey+"=\""+this.options[optKey]+"\"";
            }
        }*/
        xml+="></fmc:"+this.getTagName()+">";

        return xml;
    },

    /**
     *Get the id of this layer
     */
    getId :function (){
        return this.id;
    },
    reload : function (){
        this.getFrameworkLayer().callMethod(this.getFrameworkId(),"update");
    },
    applyUrl: function(url){
        this.url=url;
        if (this.getFrameworkLayer()!=null){
            this.getFrameworkLayer().callMethod(this.getFrameworkId(),"setAttribute","url",url);
        }
    },
    applyExtent: function(extent){
        this.extent=extent;
        if (this.getFrameworkLayer()!=null){
            this.getFrameworkLayer().callMethod(this.getFrameworkId(),"setAttribute","extent",extent);
        }
    },
    setVisible : function (visible){
        this.map.getFrameworkMap().callMethod(this.map.id + "_" + this.id, "setVisible", visible);
        this.visible = visible;
    }
});