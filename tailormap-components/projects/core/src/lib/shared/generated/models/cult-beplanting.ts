/* tslint:disable */
import { Feature } from './feature';
import { Geometry } from './geometry';
export interface CultBeplanting extends Feature {
  aanlegjaar?: number;
  afbeelding?: string;
  bbv?: string;
  begin_tijd?: string;
  beheercluster?: string;
  beheerder?: string;
  beheerder_vakgeb?: string;
  bestek_nr?: string;
  bronhouder?: string;
  buurt?: string;
  categorie?: string;
  cult_bepl_type?: string;
  domein?: string;
  eind_tijd?: string;
  fid?: number;
  functieondsteunwegdeel?: string;
  fysiekvkbegrterplus?: string;
  fysiekvkondsteunwd?: string;
  fysiekvkondsteunwdplus?: string;
  fysiekvoorkomenbegrter?: string;
  gebruiksdruk?: string;
  gemeente?: string;
  geometrie?: Geometry;
  groenobject_id?: string;
  grondslag?: string;
  harde_rand?: number;
  hoofdcategorie?: string;
  id?: number;
  imgeo_id?: string;
  in_onderzoek?: string;
  object_begin_tijd?: string;
  object_eind_tijd?: string;
  object_guid?: string;
  openbare_ruimte?: string;
  oppervlakte?: number;
  optalud?: string;
  orig_id?: number;
  ploegindeling?: string;
  rayon?: string;
  reg_begin?: string;
  reg_eind?: string;
  relatieve_hoogteligging?: number;
  status?: string;
  std_beheercluster?: string;
  std_beheerder_vakgeb?: string;
  std_cult_bepl_type?: string;
  std_domein?: string;
  std_gebruiksdruk?: string;
  std_grondslag?: string;
  std_structuurelement?: string;
  structuurelement?: string;
  subcategorie?: string;
  toelichting?: string;
  wijk?: string;
  woonplaats?: string;
  x62_cluster_aard?: string;
  x62_std_categorie?: string;
  x62_std_hoofdcategorie?: string;
  x62_std_rayon?: string;
  x62_std_subcategorie?: string;
  zachte_rand?: number;
}
