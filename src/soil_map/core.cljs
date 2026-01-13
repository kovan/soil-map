(ns soil-map.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [clojure.string :as str]))

;; Soil Organic Carbon data by country (% in topsoil)
;; Sources: FAO SOLAW, ISRIC SoilGrids, scientific literature
(def soil-organic-data
  {;; Europe
   "RUS" 2.8, "DEU" 2.1, "FRA" 1.9, "GBR" 3.2, "ITA" 1.6
   "ESP" 1.4, "POL" 1.8, "UKR" 3.1, "ROU" 2.0, "NLD" 4.2
   "BEL" 2.5, "CZE" 1.9, "GRC" 1.3, "PRT" 1.5, "SWE" 4.8
   "HUN" 2.2, "AUT" 3.0, "CHE" 3.5, "BGR" 1.8, "DNK" 2.8
   "FIN" 5.2, "SVK" 2.0, "NOR" 4.5, "IRL" 4.8, "HRV" 2.1
   "BIH" 2.3, "ALB" 1.7, "LTU" 2.4, "SVN" 3.2, "LVA" 2.6
   "EST" 3.0, "MKD" 1.9, "LUX" 2.3, "MNE" 2.5, "ISL" 6.5
   "SRB" 2.1, "BLR" 2.5, "MDA" 2.8
   ;; North America
   "USA" 2.0, "CAN" 3.5, "MEX" 1.6, "GTM" 2.2, "CUB" 2.0
   "HTI" 1.4, "DOM" 1.8, "HND" 2.0, "SLV" 1.8, "NIC" 2.2
   "CRI" 3.2, "PAN" 2.8, "JAM" 2.0, "TTO" 2.4, "BHS" 1.5
   "BLZ" 2.5
   ;; South America
   "BRA" 1.8, "ARG" 2.2, "COL" 2.5, "PER" 1.9, "VEN" 1.8
   "CHL" 2.8, "ECU" 2.6, "BOL" 1.6, "PRY" 1.4, "URY" 2.8
   "GUY" 2.2, "SUR" 2.0
   ;; Asia
   "CHN" 1.5, "IND" 0.8, "IDN" 2.2, "PAK" 0.6, "BGD" 1.2
   "JPN" 2.8, "PHL" 1.8, "VNM" 1.6, "THA" 1.4, "MMR" 1.5
   "MYS" 2.5, "NPL" 1.8, "KHM" 1.2, "LKA" 1.6, "KOR" 2.0
   "PRK" 1.8, "TWN" 2.0, "LAO" 1.8, "MNG" 1.5, "KAZ" 2.0
   "UZB" 0.9, "TKM" 0.7, "TJK" 1.2, "KGZ" 2.2, "AFG" 0.8
   "IRQ" 0.7, "IRN" 0.9, "SAU" 0.3, "YEM" 0.5, "OMN" 0.2
   "ARE" 0.2, "KWT" 0.2, "QAT" 0.2, "BHR" 0.2, "ISR" 0.8
   "JOR" 0.6, "LBN" 1.2, "SYR" 0.8, "AZE" 1.8, "GEO" 2.5
   "ARM" 2.0
   ;; Africa
   "NGA" 1.2, "EGY" 0.5, "ZAF" 1.5, "DZA" 0.8, "MAR" 1.0
   "SDN" 0.4, "ETH" 1.8, "KEN" 1.6, "TZA" 1.4, "UGA" 2.2
   "GHA" 1.5, "CIV" 1.4, "CMR" 1.8, "AGO" 1.2, "MOZ" 1.0
   "MDG" 2.0, "ZWE" 1.2, "ZMB" 1.0, "MLI" 0.4, "NER" 0.3
   "BFA" 0.5, "SEN" 0.6, "GIN" 1.5, "BEN" 1.0, "TGO" 1.2
   "SLE" 1.8, "LBR" 1.6, "MRT" 0.3, "GAB" 2.2, "COG" 2.0
   "COD" 1.8, "CAF" 1.5, "TCD" 0.5, "SOM" 0.4, "ERI" 0.6
   "BWA" 0.8, "NAM" 0.5, "SWZ" 1.8, "LSO" 1.5, "MWI" 1.4
   "RWA" 2.5, "BDI" 2.0, "DJI" 0.3, "GMB" 0.6, "GNB" 1.2
   "TUN" 0.9, "LBY" 0.3, "SSD" 1.2
   ;; Oceania
   "AUS" 1.2, "NZL" 4.5, "PNG" 2.5, "FJI" 2.8, "SLB" 2.2
   "VUT" 2.5, "NCL" 2.0, "WSM" 3.0})

;; Regional soil organic data (state/province level)
(def regional-soil-data
  {;; USA States
   "USA.AL" 1.2, "USA.AK" 4.8, "USA.AZ" 0.8, "USA.AR" 1.5, "USA.CA" 1.4
   "USA.CO" 1.8, "USA.CT" 2.8, "USA.DE" 1.6, "USA.FL" 1.9, "USA.GA" 1.1
   "USA.HI" 3.5, "USA.ID" 2.2, "USA.IL" 3.2, "USA.IN" 2.8, "USA.IA" 3.8
   "USA.KS" 2.0, "USA.KY" 1.8, "USA.LA" 1.6, "USA.ME" 3.5, "USA.MD" 1.8
   "USA.MA" 2.5, "USA.MI" 2.8, "USA.MN" 4.2, "USA.MS" 1.3, "USA.MO" 2.0
   "USA.MT" 2.5, "USA.NE" 2.4, "USA.NV" 0.7, "USA.NH" 3.2, "USA.NJ" 2.0
   "USA.NM" 0.9, "USA.NY" 2.8, "USA.NC" 1.5, "USA.ND" 3.5, "USA.OH" 2.5
   "USA.OK" 1.5, "USA.OR" 2.8, "USA.PA" 2.2, "USA.RI" 2.4, "USA.SC" 1.2
   "USA.SD" 3.0, "USA.TN" 1.6, "USA.TX" 1.2, "USA.UT" 1.4, "USA.VT" 3.5
   "USA.VA" 1.8, "USA.WA" 2.5, "USA.WV" 2.0, "USA.WI" 3.5, "USA.WY" 1.8
   ;; Canada Provinces
   "CAN.BC" 4.2, "CAN.AB" 3.8, "CAN.SK" 4.5, "CAN.MB" 4.0, "CAN.ON" 3.2
   "CAN.QC" 3.8, "CAN.NB" 3.5, "CAN.NS" 3.2, "CAN.PE" 3.0, "CAN.NL" 4.5
   "CAN.YT" 5.0, "CAN.NT" 5.5, "CAN.NU" 5.8
   ;; Australia States
   "AUS.NSW" 1.4, "AUS.VIC" 1.8, "AUS.QLD" 1.0, "AUS.SA" 0.8, "AUS.WA" 0.6
   "AUS.TAS" 3.2, "AUS.NT" 0.5, "AUS.ACT" 1.6
   ;; Germany States
   "DEU.BW" 2.0, "DEU.BY" 2.2, "DEU.BE" 1.8, "DEU.BB" 1.5, "DEU.HB" 2.0
   "DEU.HH" 1.9, "DEU.HE" 2.1, "DEU.MV" 1.8, "DEU.NI" 2.3, "DEU.NW" 2.0
   "DEU.RP" 1.9, "DEU.SL" 1.8, "DEU.SN" 1.7, "DEU.ST" 1.6, "DEU.SH" 2.5
   "DEU.TH" 1.9
   ;; France Regions
   "FRA.IDF" 1.6, "FRA.CVL" 1.8, "FRA.BFC" 2.2, "FRA.NOR" 2.0, "FRA.HDF" 1.9
   "FRA.GES" 1.8, "FRA.PDL" 2.1, "FRA.BRE" 2.5, "FRA.NAQ" 1.7, "FRA.OCC" 1.5
   "FRA.ARA" 2.0, "FRA.PAC" 1.4, "FRA.COR" 1.8
   ;; Spain Regions
   "ESP.AN" 1.2, "ESP.AR" 1.5, "ESP.AS" 3.2, "ESP.IB" 1.8, "ESP.CN" 2.0
   "ESP.CB" 2.8, "ESP.CM" 1.3, "ESP.CL" 1.5, "ESP.CT" 1.8, "ESP.EX" 1.2
   "ESP.GA" 3.5, "ESP.MD" 1.4, "ESP.MC" 1.0, "ESP.NC" 2.2, "ESP.PV" 2.5
   "ESP.RI" 1.8, "ESP.VC" 1.2
   ;; Italy Regions
   "ITA.PIE" 1.8, "ITA.VDA" 2.5, "ITA.LOM" 1.6, "ITA.TAA" 3.2, "ITA.VEN" 1.5
   "ITA.FVG" 2.0, "ITA.LIG" 1.8, "ITA.EMR" 1.7, "ITA.TOS" 1.5, "ITA.UMB" 1.6
   "ITA.MAR" 1.4, "ITA.LAZ" 1.4, "ITA.ABR" 1.8, "ITA.MOL" 1.5, "ITA.CAM" 1.3
   "ITA.PUG" 1.2, "ITA.BAS" 1.4, "ITA.CAL" 1.2, "ITA.SIC" 1.1, "ITA.SAR" 1.5
   ;; Brazil States
   "BRA.AC" 2.0, "BRA.AL" 1.2, "BRA.AP" 2.2, "BRA.AM" 2.5, "BRA.BA" 1.4
   "BRA.CE" 1.0, "BRA.DF" 1.8, "BRA.ES" 1.6, "BRA.GO" 1.6, "BRA.MA" 1.5
   "BRA.MT" 1.8, "BRA.MS" 1.7, "BRA.MG" 1.8, "BRA.PA" 2.2, "BRA.PB" 1.1
   "BRA.PR" 2.5, "BRA.PE" 1.2, "BRA.PI" 1.0, "BRA.RJ" 1.5, "BRA.RN" 0.9
   "BRA.RS" 2.2, "BRA.RO" 2.0, "BRA.RR" 2.3, "BRA.SC" 2.8, "BRA.SP" 1.6
   "BRA.SE" 1.2, "BRA.TO" 1.5
   ;; China Provinces
   "CHN.AH" 1.4, "CHN.BJ" 1.2, "CHN.CQ" 1.5, "CHN.FJ" 1.6, "CHN.GS" 1.2
   "CHN.GD" 1.8, "CHN.GX" 1.7, "CHN.GZ" 1.8, "CHN.HI" 2.0, "CHN.HE" 1.2
   "CHN.HL" 3.5, "CHN.HA" 1.3, "CHN.HB" 1.5, "CHN.HN" 1.6, "CHN.JS" 1.4
   "CHN.JX" 1.7, "CHN.JL" 2.8, "CHN.LN" 2.2, "CHN.NM" 2.0, "CHN.NX" 1.0
   "CHN.QH" 2.5, "CHN.SA" 1.3, "CHN.SD" 1.2, "CHN.SH" 1.3, "CHN.SX" 1.1
   "CHN.SC" 1.8, "CHN.TJ" 1.2, "CHN.XJ" 1.0, "CHN.XZ" 2.2, "CHN.YN" 2.0
   "CHN.ZJ" 1.5
   ;; India States
   "IND.AP" 0.6, "IND.AR" 2.0, "IND.AS" 1.5, "IND.BR" 0.7, "IND.CT" 1.0
   "IND.GA" 1.8, "IND.GJ" 0.6, "IND.HR" 0.5, "IND.HP" 1.8, "IND.JK" 1.5
   "IND.JH" 0.9, "IND.KA" 0.8, "IND.KL" 1.5, "IND.MP" 0.7, "IND.MH" 0.8
   "IND.MN" 1.8, "IND.ML" 2.2, "IND.MZ" 2.0, "IND.NL" 1.8, "IND.OR" 0.7
   "IND.PB" 0.5, "IND.RJ" 0.4, "IND.SK" 2.5, "IND.TN" 0.6, "IND.TG" 0.6
   "IND.TR" 1.2, "IND.UP" 0.5, "IND.UT" 1.5, "IND.WB" 0.8
   ;; Mexico States
   "MEX.AGU" 1.2, "MEX.BCN" 1.0, "MEX.BCS" 0.8, "MEX.CAM" 2.0, "MEX.CHP" 2.2
   "MEX.CHH" 1.2, "MEX.COA" 1.0, "MEX.COL" 1.8, "MEX.DIF" 1.5, "MEX.DUR" 1.4
   "MEX.GUA" 1.3, "MEX.GRO" 1.8, "MEX.HID" 1.6, "MEX.JAL" 1.6, "MEX.MEX" 1.5
   "MEX.MIC" 1.7, "MEX.MOR" 1.6, "MEX.NAY" 1.8, "MEX.NLE" 1.1, "MEX.OAX" 1.9
   "MEX.PUE" 1.5, "MEX.QUE" 1.3, "MEX.ROO" 2.2, "MEX.SLP" 1.4, "MEX.SIN" 1.5
   "MEX.SON" 1.0, "MEX.TAB" 2.5, "MEX.TAM" 1.3, "MEX.TLA" 1.4, "MEX.VER" 2.0
   "MEX.YUC" 1.8, "MEX.ZAC" 1.2
   ;; Japan Prefectures
   "JPN.HOK" 4.5, "JPN.AOI" 2.8, "JPN.IWT" 2.5, "JPN.MYG" 2.2, "JPN.AKT" 2.8
   "JPN.YGT" 2.5, "JPN.FKS" 2.2, "JPN.IBR" 2.0, "JPN.TCG" 2.2, "JPN.GNM" 2.5
   "JPN.STM" 1.8, "JPN.CHB" 1.8, "JPN.TKY" 1.5, "JPN.KNG" 1.8, "JPN.NGT" 3.0
   "JPN.TYM" 2.8, "JPN.ISK" 2.5, "JPN.FKI" 2.2, "JPN.YNS" 2.0, "JPN.NGN" 3.2
   "JPN.GIF" 2.2, "JPN.SZO" 2.0, "JPN.AIC" 1.8, "JPN.MIE" 2.0, "JPN.SIG" 2.2
   "JPN.KYT" 2.0, "JPN.OSK" 1.5, "JPN.HYG" 2.0, "JPN.NAR" 2.2, "JPN.WKY" 2.5
   "JPN.TTR" 2.5, "JPN.SMN" 2.2, "JPN.OKY" 2.0, "JPN.HRS" 2.0, "JPN.YGC" 2.2
   "JPN.TKS" 2.0, "JPN.KGW" 1.8, "JPN.EHM" 2.0, "JPN.KOC" 2.5, "JPN.FKO" 2.2
   "JPN.SAG" 2.5, "JPN.NGS" 2.8, "JPN.KMM" 2.5, "JPN.OIT" 2.5, "JPN.MYZ" 2.2
   "JPN.KGS" 2.5, "JPN.OKN" 2.8})

;; Region name to code mappings
(def region-name-to-code
  {;; USA States
   "Alabama" "USA.AL", "Alaska" "USA.AK", "Arizona" "USA.AZ", "Arkansas" "USA.AR"
   "California" "USA.CA", "Colorado" "USA.CO", "Connecticut" "USA.CT", "Delaware" "USA.DE"
   "Florida" "USA.FL", "Georgia" "USA.GA", "Hawaii" "USA.HI", "Idaho" "USA.ID"
   "Illinois" "USA.IL", "Indiana" "USA.IN", "Iowa" "USA.IA", "Kansas" "USA.KS"
   "Kentucky" "USA.KY", "Louisiana" "USA.LA", "Maine" "USA.ME", "Maryland" "USA.MD"
   "Massachusetts" "USA.MA", "Michigan" "USA.MI", "Minnesota" "USA.MN", "Mississippi" "USA.MS"
   "Missouri" "USA.MO", "Montana" "USA.MT", "Nebraska" "USA.NE", "Nevada" "USA.NV"
   "New Hampshire" "USA.NH", "New Jersey" "USA.NJ", "New Mexico" "USA.NM", "New York" "USA.NY"
   "North Carolina" "USA.NC", "North Dakota" "USA.ND", "Ohio" "USA.OH", "Oklahoma" "USA.OK"
   "Oregon" "USA.OR", "Pennsylvania" "USA.PA", "Rhode Island" "USA.RI", "South Carolina" "USA.SC"
   "South Dakota" "USA.SD", "Tennessee" "USA.TN", "Texas" "USA.TX", "Utah" "USA.UT"
   "Vermont" "USA.VT", "Virginia" "USA.VA", "Washington" "USA.WA", "West Virginia" "USA.WV"
   "Wisconsin" "USA.WI", "Wyoming" "USA.WY"
   ;; Canada Provinces
   "British Columbia" "CAN.BC", "Alberta" "CAN.AB", "Saskatchewan" "CAN.SK"
   "Manitoba" "CAN.MB", "Ontario" "CAN.ON", "Quebec" "CAN.QC", "Québec" "CAN.QC"
   "New Brunswick" "CAN.NB", "Nova Scotia" "CAN.NS", "Prince Edward Island" "CAN.PE"
   "Newfoundland and Labrador" "CAN.NL", "Yukon" "CAN.YT", "Yukon Territory" "CAN.YT"
   "Northwest Territories" "CAN.NT", "Nunavut" "CAN.NU"
   ;; Australia States
   "New South Wales" "AUS.NSW", "Victoria" "AUS.VIC", "Queensland" "AUS.QLD"
   "South Australia" "AUS.SA", "Western Australia" "AUS.WA", "Tasmania" "AUS.TAS"
   "Northern Territory" "AUS.NT", "Australian Capital Territory" "AUS.ACT"
   ;; Germany States
   "Baden-Württemberg" "DEU.BW", "Bavaria" "DEU.BY", "Bayern" "DEU.BY"
   "Berlin" "DEU.BE", "Brandenburg" "DEU.BB", "Bremen" "DEU.HB"
   "Hamburg" "DEU.HH", "Hesse" "DEU.HE", "Hessen" "DEU.HE"
   "Mecklenburg-Vorpommern" "DEU.MV", "Lower Saxony" "DEU.NI", "Niedersachsen" "DEU.NI"
   "North Rhine-Westphalia" "DEU.NW", "Nordrhein-Westfalen" "DEU.NW"
   "Rhineland-Palatinate" "DEU.RP", "Rheinland-Pfalz" "DEU.RP"
   "Saarland" "DEU.SL", "Saxony" "DEU.SN", "Sachsen" "DEU.SN"
   "Saxony-Anhalt" "DEU.ST", "Sachsen-Anhalt" "DEU.ST"
   "Schleswig-Holstein" "DEU.SH", "Thuringia" "DEU.TH", "Thüringen" "DEU.TH"
   ;; France Regions
   "Île-de-France" "FRA.IDF", "Centre-Val de Loire" "FRA.CVL"
   "Bourgogne-Franche-Comté" "FRA.BFC", "Normandy" "FRA.NOR", "Normandie" "FRA.NOR"
   "Hauts-de-France" "FRA.HDF", "Grand Est" "FRA.GES", "Pays de la Loire" "FRA.PDL"
   "Brittany" "FRA.BRE", "Bretagne" "FRA.BRE", "Nouvelle-Aquitaine" "FRA.NAQ"
   "Occitanie" "FRA.OCC", "Auvergne-Rhône-Alpes" "FRA.ARA"
   "Provence-Alpes-Côte d'Azur" "FRA.PAC", "Corsica" "FRA.COR", "Corse" "FRA.COR"
   ;; Spain Regions
   "Andalucía" "ESP.AN", "Andalusia" "ESP.AN", "Aragón" "ESP.AR", "Aragon" "ESP.AR"
   "Asturias" "ESP.AS", "Islas Baleares" "ESP.IB", "Balearic Islands" "ESP.IB"
   "Canarias" "ESP.CN", "Canary Islands" "ESP.CN", "Cantabria" "ESP.CB"
   "Castilla-La Mancha" "ESP.CM", "Castilla y León" "ESP.CL", "Castile and León" "ESP.CL"
   "Cataluña" "ESP.CT", "Catalonia" "ESP.CT", "Catalunya" "ESP.CT"
   "Extremadura" "ESP.EX", "Galicia" "ESP.GA", "Comunidad de Madrid" "ESP.MD"
   "Madrid" "ESP.MD", "Región de Murcia" "ESP.MC", "Murcia" "ESP.MC"
   "Navarra" "ESP.NC", "Navarre" "ESP.NC", "País Vasco" "ESP.PV", "Basque Country" "ESP.PV"
   "La Rioja" "ESP.RI", "Comunidad Valenciana" "ESP.VC", "Valencia" "ESP.VC"
   ;; Italy Regions
   "Piemonte" "ITA.PIE", "Piedmont" "ITA.PIE", "Valle d'Aosta" "ITA.VDA"
   "Lombardia" "ITA.LOM", "Lombardy" "ITA.LOM", "Trentino-Alto Adige" "ITA.TAA"
   "Veneto" "ITA.VEN", "Friuli-Venezia Giulia" "ITA.FVG", "Liguria" "ITA.LIG"
   "Emilia-Romagna" "ITA.EMR", "Toscana" "ITA.TOS", "Tuscany" "ITA.TOS"
   "Umbria" "ITA.UMB", "Marche" "ITA.MAR", "Lazio" "ITA.LAZ"
   "Abruzzo" "ITA.ABR", "Molise" "ITA.MOL", "Campania" "ITA.CAM"
   "Puglia" "ITA.PUG", "Apulia" "ITA.PUG", "Basilicata" "ITA.BAS"
   "Calabria" "ITA.CAL", "Sicilia" "ITA.SIC", "Sicily" "ITA.SIC"
   "Sardegna" "ITA.SAR", "Sardinia" "ITA.SAR"
   ;; Brazil States
   "Acre" "BRA.AC", "Alagoas" "BRA.AL", "Amapá" "BRA.AP", "Amazonas" "BRA.AM"
   "Bahia" "BRA.BA", "Ceará" "BRA.CE", "Distrito Federal" "BRA.DF"
   "Espírito Santo" "BRA.ES", "Goiás" "BRA.GO", "Maranhão" "BRA.MA"
   "Mato Grosso" "BRA.MT", "Mato Grosso do Sul" "BRA.MS", "Minas Gerais" "BRA.MG"
   "Pará" "BRA.PA", "Paraíba" "BRA.PB", "Paraná" "BRA.PR", "Pernambuco" "BRA.PE"
   "Piauí" "BRA.PI", "Rio de Janeiro" "BRA.RJ", "Rio Grande do Norte" "BRA.RN"
   "Rio Grande do Sul" "BRA.RS", "Rondônia" "BRA.RO", "Roraima" "BRA.RR"
   "Santa Catarina" "BRA.SC", "São Paulo" "BRA.SP", "Sergipe" "BRA.SE", "Tocantins" "BRA.TO"
   ;; China Provinces
   "Anhui" "CHN.AH", "Beijing" "CHN.BJ", "Chongqing" "CHN.CQ", "Fujian" "CHN.FJ"
   "Gansu" "CHN.GS", "Guangdong" "CHN.GD", "Guangxi" "CHN.GX", "Guizhou" "CHN.GZ"
   "Hainan" "CHN.HI", "Hebei" "CHN.HE", "Heilongjiang" "CHN.HL", "Henan" "CHN.HA"
   "Hubei" "CHN.HB", "Hunan" "CHN.HN", "Jiangsu" "CHN.JS", "Jiangxi" "CHN.JX"
   "Jilin" "CHN.JL", "Liaoning" "CHN.LN", "Inner Mongolia" "CHN.NM", "Ningxia" "CHN.NX"
   "Qinghai" "CHN.QH", "Shaanxi" "CHN.SA", "Shandong" "CHN.SD", "Shanghai" "CHN.SH"
   "Shanxi" "CHN.SX", "Sichuan" "CHN.SC", "Tianjin" "CHN.TJ", "Xinjiang" "CHN.XJ"
   "Tibet" "CHN.XZ", "Xizang" "CHN.XZ", "Yunnan" "CHN.YN", "Zhejiang" "CHN.ZJ"
   ;; India States
   "Andhra Pradesh" "IND.AP", "Arunachal Pradesh" "IND.AR", "Assam" "IND.AS"
   "Bihar" "IND.BR", "Chhattisgarh" "IND.CT", "Goa" "IND.GA", "Gujarat" "IND.GJ"
   "Haryana" "IND.HR", "Himachal Pradesh" "IND.HP", "Jammu and Kashmir" "IND.JK"
   "Jharkhand" "IND.JH", "Karnataka" "IND.KA", "Kerala" "IND.KL"
   "Madhya Pradesh" "IND.MP", "Maharashtra" "IND.MH", "Manipur" "IND.MN"
   "Meghalaya" "IND.ML", "Mizoram" "IND.MZ", "Nagaland" "IND.NL", "Odisha" "IND.OR"
   "Punjab" "IND.PB", "Rajasthan" "IND.RJ", "Sikkim" "IND.SK", "Tamil Nadu" "IND.TN"
   "Telangana" "IND.TG", "Tripura" "IND.TR", "Uttar Pradesh" "IND.UP"
   "Uttarakhand" "IND.UT", "West Bengal" "IND.WB"
   ;; Mexico States
   "Aguascalientes" "MEX.AGU", "Baja California" "MEX.BCN", "Baja California Sur" "MEX.BCS"
   "Campeche" "MEX.CAM", "Chiapas" "MEX.CHP", "Chihuahua" "MEX.CHH", "Coahuila" "MEX.COA"
   "Colima" "MEX.COL", "Ciudad de México" "MEX.DIF", "Durango" "MEX.DUR"
   "Guanajuato" "MEX.GUA", "Guerrero" "MEX.GRO", "Hidalgo" "MEX.HID", "Jalisco" "MEX.JAL"
   "México" "MEX.MEX", "Michoacán" "MEX.MIC", "Morelos" "MEX.MOR", "Nayarit" "MEX.NAY"
   "Nuevo León" "MEX.NLE", "Oaxaca" "MEX.OAX", "Puebla" "MEX.PUE", "Querétaro" "MEX.QUE"
   "Quintana Roo" "MEX.ROO", "San Luis Potosí" "MEX.SLP", "Sinaloa" "MEX.SIN"
   "Sonora" "MEX.SON", "Tabasco" "MEX.TAB", "Tamaulipas" "MEX.TAM", "Tlaxcala" "MEX.TLA"
   "Veracruz" "MEX.VER", "Yucatán" "MEX.YUC", "Zacatecas" "MEX.ZAC"
   ;; Japan Prefectures (with Ken/Fu/To/Do suffixes)
   "Hokkaido" "JPN.HOK", "Hokkaidō" "JPN.HOK"
   "Aomori" "JPN.AOI", "Aomori Ken" "JPN.AOI"
   "Iwate" "JPN.IWT", "Iwate Ken" "JPN.IWT"
   "Miyagi" "JPN.MYG", "Miyagi Ken" "JPN.MYG"
   "Akita" "JPN.AKT", "Akita Ken" "JPN.AKT"
   "Yamagata" "JPN.YGT", "Yamagata Ken" "JPN.YGT"
   "Fukushima" "JPN.FKS", "Fukushima Ken" "JPN.FKS"
   "Ibaraki" "JPN.IBR", "Ibaraki Ken" "JPN.IBR"
   "Tochigi" "JPN.TCG", "Tochigi Ken" "JPN.TCG"
   "Gunma" "JPN.GNM", "Gunma Ken" "JPN.GNM"
   "Saitama" "JPN.STM", "Saitama Ken" "JPN.STM"
   "Chiba" "JPN.CHB", "Chiba Ken" "JPN.CHB"
   "Tokyo" "JPN.TKY", "Tokyo To" "JPN.TKY", "Tōkyō" "JPN.TKY"
   "Kanagawa" "JPN.KNG", "Kanagawa Ken" "JPN.KNG"
   "Niigata" "JPN.NGT", "Niigata Ken" "JPN.NGT"
   "Toyama" "JPN.TYM", "Toyama Ken" "JPN.TYM"
   "Ishikawa" "JPN.ISK", "Ishikawa Ken" "JPN.ISK"
   "Fukui" "JPN.FKI", "Fukui Ken" "JPN.FKI"
   "Yamanashi" "JPN.YNS", "Yamanashi Ken" "JPN.YNS"
   "Nagano" "JPN.NGN", "Nagano Ken" "JPN.NGN"
   "Gifu" "JPN.GIF", "Gifu Ken" "JPN.GIF"
   "Shizuoka" "JPN.SZO", "Shizuoka Ken" "JPN.SZO"
   "Aichi" "JPN.AIC", "Aichi Ken" "JPN.AIC"
   "Mie" "JPN.MIE", "Mie Ken" "JPN.MIE"
   "Shiga" "JPN.SIG", "Shiga Ken" "JPN.SIG"
   "Kyoto" "JPN.KYT", "Kyoto Fu" "JPN.KYT", "Kyōto" "JPN.KYT"
   "Osaka" "JPN.OSK", "Osaka Fu" "JPN.OSK", "Ōsaka" "JPN.OSK"
   "Hyogo" "JPN.HYG", "Hyogo Ken" "JPN.HYG", "Hyōgo" "JPN.HYG"
   "Nara" "JPN.NAR", "Nara Ken" "JPN.NAR"
   "Wakayama" "JPN.WKY", "Wakayama Ken" "JPN.WKY"
   "Tottori" "JPN.TTR", "Tottori Ken" "JPN.TTR"
   "Shimane" "JPN.SMN", "Shimane Ken" "JPN.SMN"
   "Okayama" "JPN.OKY", "Okayama Ken" "JPN.OKY"
   "Hiroshima" "JPN.HRS", "Hiroshima Ken" "JPN.HRS"
   "Yamaguchi" "JPN.YGC", "Yamaguchi Ken" "JPN.YGC"
   "Tokushima" "JPN.TKS", "Tokushima Ken" "JPN.TKS"
   "Kagawa" "JPN.KGW", "Kagawa Ken" "JPN.KGW"
   "Ehime" "JPN.EHM", "Ehime Ken" "JPN.EHM"
   "Kochi" "JPN.KOC", "Kochi Ken" "JPN.KOC", "Kōchi" "JPN.KOC"
   "Fukuoka" "JPN.FKO", "Fukuoka Ken" "JPN.FKO"
   "Saga" "JPN.SAG", "Saga Ken" "JPN.SAG"
   "Nagasaki" "JPN.NGS", "Nagasaki Ken" "JPN.NGS"
   "Kumamoto" "JPN.KMM", "Kumamoto Ken" "JPN.KMM"
   "Oita" "JPN.OIT", "Oita Ken" "JPN.OIT", "Ōita" "JPN.OIT"
   "Miyazaki" "JPN.MYZ", "Miyazaki Ken" "JPN.MYZ"
   "Kagoshima" "JPN.KGS", "Kagoshima Ken" "JPN.KGS"
   "Okinawa" "JPN.OKN", "Okinawa Ken" "JPN.OKN"
   ;; China Provinces (Chinese names)
   "新疆维吾尔自治区" "CHN.XJ", "西藏自治区" "CHN.XZ", "青海省" "CHN.QH"
   "甘肃省" "CHN.GS", "四川省" "CHN.SC", "云南省" "CHN.YN"
   "内蒙古自治区" "CHN.NM", "黑龙江省" "CHN.HL", "吉林省" "CHN.JL"
   "辽宁省" "CHN.LN", "河北省" "CHN.HE", "山东省" "CHN.SD"
   "江苏省" "CHN.JS", "浙江省" "CHN.ZJ", "福建省" "CHN.FJ"
   "广东省" "CHN.GD", "广西壮族自治区" "CHN.GX", "海南省" "CHN.HI"
   "贵州省" "CHN.GZ", "湖南省" "CHN.HN", "湖北省" "CHN.HB"
   "河南省" "CHN.HA", "山西省" "CHN.SX", "陕西省" "CHN.SA"
   "宁夏回族自治区" "CHN.NX", "安徽省" "CHN.AH", "江西省" "CHN.JX"
   "北京市" "CHN.BJ", "天津市" "CHN.TJ", "上海市" "CHN.SH"
   "重庆市" "CHN.CQ", "台湾省" "CHN.TW", "香港特别行政区" "CHN.HK"
   "澳门特别行政区" "CHN.MO"})

;; Country code to name mapping
(def country-names
  {"USA" "United States", "CAN" "Canada", "AUS" "Australia"
   "DEU" "Germany", "FRA" "France", "ESP" "Spain"
   "ITA" "Italy", "BRA" "Brazil", "CHN" "China"
   "IND" "India", "MEX" "Mexico", "JPN" "Japan"})

;; Regional GeoJSON URLs
(def regional-geojson-urls
  {"USA" "https://raw.githubusercontent.com/PublicaMundi/MappingAPI/master/data/geojson/us-states.json"
   "CAN" "https://raw.githubusercontent.com/codeforamerica/click_that_hood/master/public/data/canada.geojson"
   "AUS" "https://raw.githubusercontent.com/rowanhogan/australian-states/master/states.geojson"
   "DEU" "https://raw.githubusercontent.com/isellsoap/deutschlandGeoJSON/main/2_bundeslaender/4_niedrig.geo.json"
   "FRA" "https://raw.githubusercontent.com/gregoiredavid/france-geojson/master/regions-version-simplifiee.geojson"
   "ESP" "https://raw.githubusercontent.com/codeforgermany/click_that_hood/main/public/data/spain-communities.geojson"
   "ITA" "https://raw.githubusercontent.com/openpolis/geojson-italy/master/geojson/limits_IT_regions.geojson"
   "BRA" "https://raw.githubusercontent.com/codeforamerica/click_that_hood/master/public/data/brazil-states.geojson"
   "CHN" "https://raw.githubusercontent.com/longwosion/geojson-map-china/master/china.json"
   "IND" "https://raw.githubusercontent.com/geohacker/india/master/state/india_telengana.geojson"
   "MEX" "https://raw.githubusercontent.com/angelnmara/geojson/master/mexicoHigh.json"
   "JPN" "https://raw.githubusercontent.com/dataofjapan/land/master/japan.geojson"})

;; Color scale for choropleth (10-step gradient from green to red)
(defn get-color [value]
  (cond
    (nil? value) "#ccc"
    (>= value 5) "#1B5E20"   ; Darkest green - peatland
    (>= value 4) "#2E7D32"   ; Dark green
    (>= value 3) "#4CAF50"   ; Green - excellent
    (>= value 2.5) "#66BB6A" ; Medium green
    (>= value 2) "#8BC34A"   ; Light green - good
    (>= value 1.5) "#CDDC39" ; Yellow-green
    (>= value 1) "#FFC107"   ; Amber - moderate
    (>= value 0.5) "#FF9800" ; Orange - poor
    (>= value 0.25) "#FF5722" ; Deep orange
    (> value 0) "#F44336"    ; Red - very poor
    :else "#ccc"))

;; State
(defonce app-state (r/atom {:map nil
                            :country-layer nil
                            :regional-layer nil
                            :current-mode :countries  ; :countries or :regional
                            :hovered-country nil
                            :hovered-region nil
                            :hovered-value nil}))

;; Reference to info control container for manual re-renders
(defonce info-control-container (atom nil))

;; Style function for countries
(defn country-style [feature]
  (let [country-code (-> feature .-properties (aget "ISO3166-1-Alpha-3"))
        value (get soil-organic-data country-code)
        color (get-color value)]
    #js {:fillColor color
         :weight 1
         :opacity 1
         :color "#fff"
         :fillOpacity 0.85}))

;; Helper to get region code from feature properties
(defn get-region-code [props]
  ;; Try all property values to find a matching region name
  (let [keys (js/Object.keys props)]
    (loop [i 0]
      (if (>= i (.-length keys))
        nil
        (let [key (aget keys i)
              val (aget props key)]
          (if (string? val)
            (if-let [code (or (get region-name-to-code val)
                              (get region-name-to-code (str/trim val)))]
              code
              (recur (inc i)))
            (recur (inc i))))))))

;; Helper to get country code from region code
(defn region-to-country [region-code]
  (when region-code
    (first (str/split region-code #"\."))))

;; Style function for regional GeoJSON
(defn regional-style [feature]
  (let [props (.-properties feature)
        region-code (get-region-code props)
        value (when region-code (get regional-soil-data region-code))
        country-code (region-to-country region-code)
        final-value (or value (get soil-organic-data country-code))]
    #js {:fillColor (get-color final-value)
         :weight 0.5
         :opacity 1
         :color "#fff"
         :fillOpacity 0.85}))

;; Highlight style
(def highlight-style
  #js {:weight 3
       :color "#333"
       :fillOpacity 0.95})

;; Reset style for country layer
(defn reset-country-style [layer]
  (when-let [country-layer (:country-layer @app-state)]
    (.resetStyle country-layer layer)))

;; Reset style for regional layer
(defn reset-regional-style [layer]
  (.setStyle layer (regional-style (.-feature layer))))

;; Mouse event handlers for country layer
(defn on-each-country [feature layer]
  (let [country-code (-> feature .-properties (aget "ISO3166-1-Alpha-3"))
        country-name (-> feature .-properties (aget "name"))
        value (get soil-organic-data country-code)]
    (.on layer "mouseover" (fn [_e]
                             (.setStyle layer highlight-style)
                             (.bringToFront layer)
                             (swap! app-state assoc
                                    :hovered-country country-name
                                    :hovered-region nil
                                    :hovered-value value)))
    (.on layer "mouseout" (fn [_e]
                            (reset-country-style layer)
                            (swap! app-state assoc
                                   :hovered-country nil
                                   :hovered-value nil)))
    (.on layer "click" (fn [_e]
                         (when-let [m (:map @app-state)]
                           (.fitBounds m (.getBounds layer)))))))

;; Mouse event handlers for regional layer
(defn on-each-region [feature layer]
  (let [props (.-properties feature)
        region-name (or (aget props "name")
                        (aget props "NAME")
                        (aget props "NAME_1")
                        (aget props "nom")
                        (aget props "Name")
                        (aget props "reg_name")
                        (aget props "nam"))
        region-code (get-region-code props)
        country-code (region-to-country region-code)
        country-name (get country-names country-code)
        value (or (get regional-soil-data region-code)
                  (get soil-organic-data country-code))]
    (.on layer #js {:mouseover (fn [_e]
                                  (.setStyle layer highlight-style)
                                  (.bringToFront layer)
                                  (swap! app-state assoc
                                         :hovered-country country-name
                                         :hovered-region region-name
                                         :hovered-value value))
                    :mouseout (fn [_e]
                                (reset-regional-style layer)
                                (swap! app-state assoc
                                       :hovered-country nil
                                       :hovered-region nil
                                       :hovered-value nil))
                    :click (fn [_e]
                             (when-let [m (:map @app-state)]
                               (.fitBounds m (.getBounds layer))))})))

;; Info control component - renders current hover state
(defn info-control []
  (let [{:keys [hovered-country hovered-region hovered-value current-mode]} @app-state]
    [:div.info
     [:h4 "Soil Organic Matter"]
     (if (or hovered-country hovered-region)
       [:<>
        [:div.region-name (or hovered-region hovered-country)]
        (when (and hovered-region hovered-country)
          [:div {:style {:font-size "13px" :color "#666" :margin-top "2px"}}
           hovered-country])
        [:div.percentage
         (if hovered-value
           (str hovered-value "%")
           "No data")]]
       [:p (str "Hover over a " (if (= current-mode :regional) "region" "country"))])]))

;; Soil quality legend - explains what the percentages mean
(def soil-quality-categories
  [{:range ">5%" :category "Peatland/Organic Soil" :description "Wetlands, bogs, exceptional fertility" :color "#2E7D32"}
   {:range "3-5%" :category "Excellent" :description "Prime agricultural land" :color "#4CAF50"}
   {:range "2-3%" :category "Good" :description "Productive farmland" :color "#8BC34A"}
   {:range "1-2%" :category "Moderate" :description "Suitable for agriculture with management" :color "#FFC107"}
   {:range "0.5-1%" :category "Poor" :description "Marginal land, limited agriculture" :color "#FF9800"}
   {:range "<0.5%" :category "Very Poor" :description "Desert/arid, not suitable for crops" :color "#F44336"}])

(defn soil-quality-legend []
  [:div {:style {:background "rgba(255,255,255,0.95)"
                 :padding "12px 14px"
                 :border-radius "8px"
                 :box-shadow "0 2px 8px rgba(0,0,0,0.2)"
                 :max-width "280px"
                 :font-size "12px"}}
   [:h4 {:style {:margin "0 0 10px" :font-size "13px" :color "#333"}} "Soil Quality Guide"]
   (for [{:keys [range category description color]} soil-quality-categories]
     ^{:key category}
     [:div {:style {:display "flex" :align-items "flex-start" :margin-bottom "8px"}}
      [:div {:style {:width "12px"
                     :height "12px"
                     :border-radius "50%"
                     :background-color color
                     :margin-right "8px"
                     :margin-top "2px"
                     :flex-shrink "0"}}]
      [:div
       [:div {:style {:font-weight "bold" :color "#333"}}
        (str range " - " category)]
       [:div {:style {:color "#666" :font-size "11px"}}
        description]]])])

;; Layer toggle control
(defn layer-toggle []
  (let [current-mode (:current-mode @app-state)]
    [:div {:style {:background "rgba(255,255,255,0.95)"
                   :padding "10px 14px"
                   :border-radius "8px"
                   :box-shadow "0 2px 8px rgba(0,0,0,0.2)"}}
     [:h4 {:style {:margin "0 0 8px" :font-size "13px" :color "#333"}} "View Mode"]
     [:label {:style {:display "flex" :align-items "center" :cursor "pointer"
                      :padding "4px 0" :font-size "13px" :color "#555"}}
      [:input {:type "radio"
               :name "layer"
               :checked (= current-mode :countries)
               :on-change #(swap! app-state assoc :current-mode :countries)
               :style {:margin-right "8px"}}]
      "Countries"]
     [:label {:style {:display "flex" :align-items "center" :cursor "pointer"
                      :padding "4px 0" :font-size "13px" :color "#555"}}
      [:input {:type "radio"
               :name "layer"
               :checked (= current-mode :regional)
               :on-change #(swap! app-state assoc :current-mode :regional)
               :style {:margin-right "8px"}}]
      "Regions/States"]]))

;; Title control component
(defn title-control []
  [:div.title-control
   [:h1 "World Soil Organic Matter"]
   [:p "Average organic percentage in topsoil"]])

;; Sources control - data sources and methodology
(defn sources-control []
  (let [collapsed? (r/atom true)]
    (fn []
      [:div {:style {:background "rgba(255,255,255,0.95)"
                     :padding "12px 16px"
                     :border-radius "8px"
                     :box-shadow "0 2px 8px rgba(0,0,0,0.2)"
                     :max-width "320px"
                     :font-size "12px"}}
       [:h4 {:style {:margin "0 0 10px"
                     :font-size "14px"
                     :color "#333"
                     :cursor "pointer"
                     :display "flex"
                     :justify-content "space-between"
                     :align-items "center"}
             :on-click #(swap! collapsed? not)}
        "Data Sources"
        [:span {:style {:font-size "10px"
                        :transition "transform 0.2s"
                        :transform (if @collapsed? "rotate(-90deg)" "rotate(0)")}}
         "\u25BC"]]
       (when-not @collapsed?
         [:div {:style {:color "#555" :line-height "1.6"}}
          [:div {:style {:font-weight "bold" :margin-bottom "5px"}} "Primary Sources:"]
          [:ul {:style {:margin "8px 0" :padding-left "18px"}}
           [:li [:a {:href "https://www.fao.org/global-soil-partnership"
                     :target "_blank"
                     :style {:color "#5D4037" :text-decoration "none"}}
                 "FAO Global Soil Partnership"]]
           [:li [:a {:href "https://soilgrids.org/"
                     :target "_blank"
                     :style {:color "#5D4037" :text-decoration "none"}}
                 "ISRIC SoilGrids"]]
           [:li [:a {:href "https://www.nrcs.usda.gov/wps/portal/nrcs/main/soils/survey/"
                     :target "_blank"
                     :style {:color "#5D4037" :text-decoration "none"}}
                 "USDA Web Soil Survey"]]]
          [:div {:style {:font-weight "bold" :margin "10px 0 5px"}} "Methodology:"]
          [:p {:style {:margin "5px 0"}}
           "Values represent average soil organic carbon percentage in topsoil (0-30cm depth)."]
          [:div {:style {:margin-top "10px"
                         :padding-top "10px"
                         :border-top "1px solid #e0e0e0"
                         :font-size "11px"
                         :color "#888"}}
           "Data compiled from multiple sources and may vary from local measurements. Last updated: 2024"]])])))


;; Main app component
(defn app []
  [:div#map])

;; Create Leaflet control from React component
(defn create-control [component position & {:keys [on-add]}]
  (let [Control (.-Control js/L)
        control-class (.extend Control
                               #js {:onAdd (fn [_map]
                                             (let [div (.create js/L.DomUtil "div")]
                                               (.disableClickPropagation js/L.DomEvent div)
                                               (.disableScrollPropagation js/L.DomEvent div)
                                               (when on-add (on-add div))
                                               (rdom/render [component] div)
                                               div))})]
    (control-class. #js {:position position})))

;; Load regional GeoJSON data
(defn load-regional-data [m]
  (let [regional-group (.layerGroup js/L)]
    ;; Load all regional GeoJSON files
    (doseq [[_country-code url] regional-geojson-urls]
      (-> (js/fetch url)
          (.then #(.json %))
          (.then (fn [data]
                   (let [layer (.geoJson js/L data #js {:style regional-style
                                                        :onEachFeature on-each-region})]
                     (.eachLayer layer (fn [l] (.addLayer regional-group l))))))
          (.catch #(js/console.error "Error loading regional GeoJSON:" %))))
    regional-group))

;; Switch between country and regional layers
(defn switch-layers [mode]
  (let [{:keys [map country-layer regional-layer]} @app-state]
    (when map
      (case mode
        :countries
        (do
          (when regional-layer (.removeFrom regional-layer map))
          (when country-layer (.addTo country-layer map)))
        :regional
        (do
          (when country-layer (.removeFrom country-layer map))
          (when regional-layer (.addTo regional-layer map)))))))

;; Initialize the map
(defn init-map []
  (let [m (.map js/L "map" #js {:center #js [30 0]
                                :zoom 2
                                :minZoom 2
                                :maxBounds #js [#js [-90 -180] #js [90 180]]})]
    ;; Add tile layer
    (.addTo (.tileLayer js/L
                        "https://{s}.basemaps.cartocdn.com/light_nolabels/{z}/{x}/{y}{r}.png"
                        #js {:attribution "Map data &copy; OpenStreetMap, Tiles &copy; Carto"
                             :subdomains "abcd"
                             :maxZoom 19})
            m)

    ;; Store map reference
    (swap! app-state assoc :map m)
    ;; Expose map to window for debugging
    (set! (.-soilMap js/window) m)

    ;; Add controls
    (.addTo (create-control title-control "topleft") m)
    (.addTo (create-control layer-toggle "topleft") m)
    (.addTo (create-control sources-control "bottomleft") m)
    (.addTo (create-control info-control "topright"
                            :on-add #(reset! info-control-container %)) m)
    (.addTo (create-control soil-quality-legend "bottomright") m)

    ;; Load country GeoJSON
    (-> (js/fetch "https://raw.githubusercontent.com/datasets/geo-countries/master/data/countries.geojson")
        (.then #(.json %))
        (.then (fn [data]
                 (let [country-layer (.geoJson js/L data #js {:style country-style
                                                               :onEachFeature on-each-country
                                                               :interactive true})]
                   (.addTo country-layer m)
                   (swap! app-state assoc :country-layer country-layer)

                   ;; Load regional data (but don't add to map yet)
                   (let [regional-layer (load-regional-data m)]
                     (swap! app-state assoc :regional-layer regional-layer))

                   ;; Watch for mode changes
                   (add-watch app-state :mode-change
                              (fn [_ _ old-state new-state]
                                (when (not= (:current-mode old-state) (:current-mode new-state))
                                  (switch-layers (:current-mode new-state))
                                  ;; Re-render layer toggle
                                  (when-let [toggle-div (.querySelector js/document "[style*='View Mode']")]
                                    (rdom/render [layer-toggle] (.-parentNode toggle-div))))))

                   ;; Update info control reactively
                   (add-watch app-state :info-update
                              (fn [_ _ _old-state _new-state]
                                (when-let [container @info-control-container]
                                  (rdom/render (info-control) container)))))))
        (.catch #(js/console.error "Error loading GeoJSON:" %)))))

;; Entry point
(defn init []
  (rdom/render [app] (.getElementById js/document "app"))
  ;; Initialize map after DOM is ready
  (js/setTimeout init-map 100))
