/* tslint:disable */
import { Feature } from './feature';
import { Geometry } from './geometry';
import { RioolputInspectie } from './rioolput-inspectie';
import { RioolputPlanning } from './rioolput-planning';
export interface Rioolput extends Feature {
  aanlegjaar?: number;
  aansluitend_stelseltype?: string;
  aant_aansl_inw_recr?: number;
  aant_aansl_inwoner?: number;
  aantal_aansl_bedrijven?: number;
  aantal_aansl_diversen?: number;
  aantal_aansl_horeca?: number;
  aantal_aansl_kolken?: number;
  aantal_aansl_woningen?: number;
  aantal_aansl_woonboten?: number;
  aantal_pompen?: number;
  afbeelding?: string;
  afspraak_waterschap?: string;
  afwijkendedieptelegging?: number;
  bbv?: string;
  begin_tijd?: string;
  beheercluster?: string;
  beheerder?: string;
  beheerder_vakgeb?: string;
  bergend_oppervlak?: number;
  bestek?: string;
  bestek_nr?: string;
  bouwjaar_pomp?: number;
  bovengrondszichtbaar?: string;
  breedte?: number;
  breedte_toegang?: number;
  bronhouder?: string;
  buurt?: string;
  categorie?: string;
  continue_lozing?: number;
  dab_scheur?: number;
  dac_breuk_instorting?: number;
  dad_defect_metselwerk?: number;
  daf_schade_oppervlak?: number;
  datum_maaiveldhoogte?: string;
  dba_wortels?: number;
  dbd_binnendringen_grond?: number;
  dbf_infiltratie?: number;
  diameter?: number;
  diameter_doorlaat?: number;
  diameter_persaansluiting?: number;
  diepte?: number;
  domein?: string;
  drempel_breedte?: number;
  drempelhoogte_nap?: number;
  eind_tijd?: string;
  funderingstype?: string;
  geleiding?: string;
  gem_emissie_jaar?: number;
  gem_emissie_jaar_bzv?: number;
  gemeente?: string;
  geometrie?: Geometry;
  geonauwkeurigheidxy?: number;
  geotextiel_inf_deel?: string;
  grondeigendom?: string;
  grondsoort?: string;
  grondwaterstand?: number;
  hoofdcategorie?: string;
  hoogte_put?: number;
  hoogteligging_doorlaat?: number;
  hoogteligging_obl?: number;
  hoogwaterpeil?: number;
  hulpputcode?: string;
  id?: number;
  imgeo_id?: string;
  in_onderzoek?: string;
  inschakelpeil?: number;
  inspectiedatum?: string;
  inspecties?: Array<RioolputInspectie>;
  int_putmateriaal?: string;
  invoer?: number;
  kleptype?: string;
  knoopnummer?: string;
  laagwaterpeil?: number;
  lengte?: number;
  lengte_putdeel?: number;
  lengte_toegang?: number;
  leverancier_fabrikant?: string;
  locatie?: string;
  maaiveldhoogte?: number;
  maaiveldhoogte_actueel?: number;
  maaiveldhoogte_ontwerp?: number;
  mantoegankelijk?: string;
  materiaal_gwsw?: string;
  materiaal_inf_deel?: string;
  materiaal_put?: string;
  niv_buitenwater?: number;
  object_begin_tijd?: string;
  object_eind_tijd?: string;
  object_guid?: string;
  onderhoudsfirma?: string;
  ontv_toelaatbaar_peilst?: number;
  ontv_waterpeil?: number;
  ontw_doorl_grond?: number;
  ontw_hgt_grondwaterstand?: number;
  ontw_oppervlak?: number;
  ontwerppeil_ontv_water?: number;
  openbare_ruimte?: string;
  ophanginrichting?: string;
  opp_water_straat?: number;
  planningen?: Array<RioolputPlanning>;
  pompcapaciteit?: number;
  pomptype?: string;
  porositeit?: number;
  prognose_draaiuren?: number;
  putafmeting?: string;
  putcode?: string;
  putdekseltype?: string;
  putdiepte?: number;
  rayon?: string;
  reg_begin?: string;
  reg_eind?: string;
  reinigingscyclus?: string;
  reinigingsjaar?: string;
  relatieve_hoogteligging?: number;
  revisietekening?: string;
  rioolputhfdtype_gwsw?: string;
  rioolputsubtype_gwsw?: string;
  rioolputtype?: string;
  rioolputtype_gwsw?: string;
  rl_voorziening_id?: string;
  status?: string;
  std_beheercluster?: string;
  std_beheerder_vakgeb?: string;
  std_domein?: string;
  std_funderingstype?: string;
  std_grondsoort?: string;
  std_kleptype?: string;
  std_materiaal_put?: string;
  std_putdekseltype?: string;
  std_rioolputtype?: string;
  std_structuurelement?: string;
  stelsel_id?: string;
  strategisch?: string;
  structuurelement?: string;
  subcategorie?: string;
  sw_gebied_id?: string;
  tekening?: string;
  telemetrie?: string;
  terugslagklep?: string;
  toelichting?: string;
  type_leidingelement?: string;
  type_put_plus?: string;
  uitschakelpeil?: number;
  verharding_wegdek?: string;
  volgend_reinigingsjaar?: string;
  vorm_gwsw?: string;
  vorm_toegang?: string;
  wanddikte?: number;
  waterpeil?: number;
  wijk?: string;
  woonplaats?: string;
  x62_cluster_aard?: string;
  x62_funderingstype_gwsw?: string;
  x62_infiltratieputtype?: string;
  x62_lengte_deksel?: number;
  x62_lozingswerktype?: string;
  x62_piek_emissie_10_jaar?: number;
  x62_piek_emissie_10_jaar_bzv?: number;
  x62_piek_emissie_2_jaar?: number;
  x62_piek_emissie_2_jaar_bzv?: number;
  x62_piek_emissie_5_jaar?: number;
  x62_piek_emissie_5_jaar_bzv?: number;
  x62_pompopstelling?: string;
  x62_standaard_lozingswerktype?: string;
  x62_std_categorie?: string;
  x62_std_hoofdcategorie?: string;
  x62_std_infiltratieputtype?: string;
  x62_std_rayon?: string;
  x62_std_subcategorie?: string;
}
