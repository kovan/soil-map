(ns soil-map.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]))

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

;; Color scale for choropleth (brown gradient - darker = more organic matter)
(defn get-color [value]
  (cond
    (nil? value) "#ccc"
    (>= value 5) "#3E2723"
    (>= value 4) "#4E342E"
    (>= value 3) "#5D4037"
    (>= value 2.5) "#6D4C41"
    (>= value 2) "#795548"
    (>= value 1.5) "#8D6E63"
    (>= value 1) "#A1887F"
    (>= value 0.5) "#BCAAA4"
    (> value 0) "#D7CCC8"
    :else "#EFEBE9"))

;; Legend data
(def legend-items
  [{:color "#3E2723" :label ">5%"}
   {:color "#4E342E" :label "4-5%"}
   {:color "#5D4037" :label "3-4%"}
   {:color "#6D4C41" :label "2.5-3%"}
   {:color "#795548" :label "2-2.5%"}
   {:color "#8D6E63" :label "1.5-2%"}
   {:color "#A1887F" :label "1-1.5%"}
   {:color "#BCAAA4" :label "0.5-1%"}
   {:color "#D7CCC8" :label "0-0.5%"}
   {:color "#ccc" :label "No data"}])

;; State
(defonce app-state (r/atom {:map nil
                            :geojson-layer nil
                            :hovered-country nil
                            :hovered-value nil}))

;; Style function for countries
(defn country-style [feature]
  (let [country-code (-> feature .-properties (aget "ISO3166-1-Alpha-3"))
        value (get soil-organic-data country-code)]
    #js {:fillColor (get-color value)
         :weight 1
         :opacity 1
         :color "#fff"
         :fillOpacity 0.85}))

;; Highlight style
(def highlight-style
  #js {:weight 3
       :color "#333"
       :fillOpacity 0.95})

;; Reset style
(defn reset-style [layer]
  (when-let [geojson-layer (:geojson-layer @app-state)]
    (.resetStyle geojson-layer layer)))

;; Mouse event handlers
(defn on-each-feature [feature layer]
  (let [country-code (-> feature .-properties (aget "ISO3166-1-Alpha-3"))
        country-name (-> feature .-properties (aget "ADMIN"))
        value (get soil-organic-data country-code)]
    (.on layer #js {:mouseover (fn [_e]
                                  (.setStyle layer highlight-style)
                                  (.bringToFront layer)
                                  (swap! app-state assoc
                                         :hovered-country country-name
                                         :hovered-value value))
                    :mouseout (fn [_e]
                                (reset-style layer)
                                (swap! app-state assoc
                                       :hovered-country nil
                                       :hovered-value nil))
                    :click (fn [_e]
                             (when-let [m (:map @app-state)]
                               (.fitBounds m (.getBounds layer))))})))

;; Info control component
(defn info-control []
  (let [{:keys [hovered-country hovered-value]} @app-state]
    [:div.info
     [:h4 "Soil Organic Matter"]
     (if hovered-country
       [:<>
        [:div.region-name hovered-country]
        [:div.percentage
         (if hovered-value
           (str hovered-value "%")
           "No data")]]
       [:p "Hover over a country"])]))

;; Legend control component
(defn legend-control []
  [:div.legend
   [:h4 "Organic Matter %"]
   (for [{:keys [color label]} legend-items]
     ^{:key label}
     [:div.legend-item
      [:div.legend-color {:style {:background-color color}}]
      [:span label]])])

;; Title control component
(defn title-control []
  [:div.title-control
   [:h1 "World Soil Organic Matter"]
   [:p "Average organic percentage in topsoil"]])

;; Main app component
(defn app []
  [:div#map])

;; Create Leaflet control from React component
(defn create-control [component position]
  (let [Control (.-Control js/L)
        control-class (.extend Control
                               #js {:onAdd (fn [_map]
                                             (let [div (.create js/L.DomUtil "div")]
                                               (.disableClickPropagation js/L.DomEvent div)
                                               (.disableScrollPropagation js/L.DomEvent div)
                                               (rdom/render [component] div)
                                               div))})]
    (control-class. #js {:position position})))

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

    ;; Add controls
    (.addTo (create-control title-control "topleft") m)
    (.addTo (create-control info-control "topright") m)
    (.addTo (create-control legend-control "bottomright") m)

    ;; Load GeoJSON
    (-> (js/fetch "https://raw.githubusercontent.com/datasets/geo-countries/master/data/countries.geojson")
        (.then #(.json %))
        (.then (fn [data]
                 (let [geojson-layer (.geoJson js/L data #js {:style country-style
                                                              :onEachFeature on-each-feature})]
                   (.addTo geojson-layer m)
                   (swap! app-state assoc :geojson-layer geojson-layer)
                   ;; Update info control reactively
                   (add-watch app-state :info-update
                              (fn [_ _ old-state new-state]
                                (when (or (not= (:hovered-country old-state) (:hovered-country new-state))
                                          (not= (:hovered-value old-state) (:hovered-value new-state)))
                                  ;; Re-render info control
                                  (when-let [info-div (.querySelector js/document ".info")]
                                    (rdom/render [info-control] (.-parentNode info-div)))))))))
        (.catch #(js/console.error "Error loading GeoJSON:" %)))))

;; Entry point
(defn init []
  (rdom/render [app] (.getElementById js/document "app"))
  ;; Initialize map after DOM is ready
  (js/setTimeout init-map 100))
