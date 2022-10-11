INSERT INTO category (name) VALUES("Sikkerhed og brugerstyring");
INSERT INTO category (name) VALUES("Logning");
INSERT INTO category (name) VALUES("Arkivering og statistik");
INSERT INTO category (name) VALUES("Lovgivning");
INSERT INTO category (name) VALUES("Ejerskab af løsning og data");
INSERT INTO category (name) VALUES("Standarder og klassifikationer");
INSERT INTO category (name) VALUES("Integrationer");
INSERT INTO category (name) VALUES("Miljøer");

INSERT INTO tag (name, question) VALUES("Overskrift 1", "Spørgsmål 1?");
INSERT INTO tag (name, question) VALUES("Overskrift 2", "Spørgsmål 2?");
INSERT INTO tag (name, question) VALUES("Overskrift 3", "Spørgsmål 3?");

INSERT INTO domain (name) VALUES("00 - Kommunens styrelse");
INSERT INTO domain (name) VALUES("01 - Fysisk planlægning og naturbeskyttelse");
INSERT INTO domain (name) VALUES("02 - Byggeri");
INSERT INTO domain (name) VALUES("03 - Boliger");
INSERT INTO domain (name) VALUES("04 - Parker, fritids-/idrætsanlæg og landskabspleje mv.");
INSERT INTO domain (name) VALUES("05 - Veje og trafik");
INSERT INTO domain (name) VALUES("06 - Spildevand og vandløb");
INSERT INTO domain (name) VALUES("07 - Affald og genanvendelse");

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
	"Løsningen skal understøtte login via rammearkitekturen", "36074051", "HIGH", "Login til Løsningens brugergrænseflade skal foregå via den infrastruktur der konkretiserer den fælleskommunale rammearkitektur, specifikt via de Støttesystemernes ’Adgangsstyring for Brugere’.

Indtil Støttesystemerne er leveret, kan løsningen håndtere login via en SAML 2.0 baseret integration til Kundens AD FS.

Det betyder at Løsningen skal fungere som en SAML 2.0 Service Provider, og overholde de integrationsvilkår [RA-VILKAAR] som den fælleskommunale rammearkitektur stiller til aktører af typen Brugervendt System.", 0, 1);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
	"Løsningen skal understøtte rettighedsstyring via rammearkitekturen", "36074051", "HIGH", "Brugernes rettigheder i Løsningen skal administreres via rammearkitekturen, hvilket betyder at Løsningen skal registreres i Støttesystemet Administrationsmodul, og at Løsningens rettigheder (roller og evt dataafgrænsninger) skal registreres i samme Administrationsmodul, så Kunden kan håndtere rettighedsstyring via rammearkitekturen.

Indtil Støttesystemerne er leveret, kan brugernes rettigheder udstilles direkte via Kundens AD FS som claims i det SAML 2.0 token som udstedes af denne.

Det betyder også at Løsningen ikke må stille krav til administration af adgange og rettigheder inde i selve Løsningen.", 0, 1);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Logning og overvågning af sikkerhedshændelser", "36074051", "HIGH", "Løsningen skal logge alle sikkerhedsrelaterede hændelser, herunder

Alle forsøg på login, hvad end de lykkedes eller fejler
Alle adgange til systemets snitflader (fx fra andre it-systemer), hvad end de lykkedes eller ej
Alle adgange til følsomme data, med sporbarhed til hvem der tilgik data
Alle autorisationsfejl mod eksterne systemer, herunder afvisninger pga. forkerte/udløbne akkreditiver
Alle tilgange til systemet foretaget af Leverandøren, herunder hvilke data som Leverandøren har haft adgang til.

Løsningen skal løbende overvåge sikkerhedsloggen, og alarmere i tilfælde af mistænksom adfærd", 0, 2);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Logningskrav fra it-sikkerhedsbekendtgørelsen", "36074051", "HIGH", "Løsningen skal logge de oplysninger der fremgår af §19 i [IT-SIK-BEKENDT]", 0, 2);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Arkivering hos Statens Arkiver", "36074051", "HIGH", "Løsningen skal understøtte bekendtgørelse nr. 1007 af 20. august 2010 fra Statens Arkiver [ARKIV-BEKENDT].

Aflevering til offentligt arkiv vil være omfattet af kundebetaling. Angiv i kommentarfeltet hvad betalingen maksimalt kan udgøre i kr. pr. arkiveringsversion.

I forbindelse med fejl på udtræk laver Leverandøren, efter normal praksis, efterfølgende rettelse og nyt udtræk uden beregning.

Leverandøren dækker udgifter i forbindelse med test af arkiveringsversioner der ikke kan godkendes af Syddjurs Kommunearkiv og dermed ikke overholder afleveringsbestemmelsen udstedt af Syddjurs Kommunearkiv samt bekendtgørelse nr. 1007 af 20. august 2010.

Syddjurs Kommunearkiv har outsourcet visse arkivopgaver til Aalborg Stadsarkiv, hvorfor Leverandøren accepterer at Aalborg Stadsarkiv kan fungere som Syddjurs Kommunearkivs proxy.", 0, 3);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Overholdelse af gældende lovgivning", "36074051", "HIGH", "Løsningen skal til enhver tid overholde gældende lovgivning, herunder Persondataloven, Databeskyttelsesforordningen, It-sikkerhedsbekendtgørelsen samt gældende lovgivning indenfor sundhed- og omsorgsområdet.

Dette inkluderer også sletning af data i Løsningen, i henhold til lovgivningens krav om dette.", 0, 4);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Ejerskab af data", "36074051", "HIGH", "Kunden ejer de forretningsdata der skabes i Løsningen, herunder, men ikke begrænset til, alle registreringer om Parter i systemet.", 0, 5);

INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(1, 1);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(2, 1);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(3, 2);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(4, 2);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(5, 3);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(6, 3);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(7, 4);

INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(1, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(2, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(3, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(4, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(5, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(6, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(7, 3);

INSERT INTO global_editor (user_id, cvr) VALUES ('user1', '36074051');

INSERT INTO identity_provider (name, cvr, entity_id, metadata) VALUES ('Test AD FS v1', '36074051', 'http://adfs.digital-identity.dk/adfs/services/trust', 'https://adfs.digital-identity.dk/federationmetadata/2007-06/federationmetadata.xml');

INSERT INTO purchase (id, cvr, title, status, email, start_time, end_time, description, questionnaire_filled_out) VALUES ('1', '36074051', 'Purchase 1', 'ACTIVE', 'test@test.dk', '2017-07-21 09:54:33', '2029-08-19 09:54:00', 'asdfsd fsd fsad fsd f', '1');

INSERT INTO purchase_vendor (id, name, email, username, password, timestamp, purchase_id) VALUES ('1', 'Fancy Vendor Name', 'email@test.dk', 'vendor12345', 'Test1234', '2017-07-21 10:56:04', '1');

INSERT INTO purchase_requirement (id, requirement_id, purchase_id, name, importance, description, category_id) VALUES ('1', '1', '1', 'Løsningen skal understøtte login via rammearkitekturen', 'HIGH', 'Login til Løsningens brugergrænseflade skal foregå via den infrastruktur der konkretiserer den fælleskommunale rammearkitektur, specifikt via de Støttesystemernes ’Adgangsstyring for Brugere’.\n\nIndtil Støttesystemerne er leveret, kan løsningen håndtere login via en SAML 2.0 baseret integration til Kundens AD FS.\n\nDet betyder at Løsningen skal fungere som en SAML 2.0 Service Provider, og overholde de integrationsvilkår [RA-VILKAAR] som den fælleskommunale rammearkitektur stiller til aktører af typen Brugervendt System.', '1');
INSERT INTO purchase_requirement (id, requirement_id, purchase_id, name, importance, description, category_id) VALUES ('2', '2', '1', 'Løsningen skal understøtte rettighedsstyring via rammearkitekturen', 'HIGH', 'Brugernes rettigheder i Løsningen skal administreres via rammearkitekturen, hvilket betyder at Løsningen skal registreres i Støttesystemet Administrationsmodul, og at Løsningens rettigheder (roller og evt dataafgrænsninger) skal registreres i samme Administrationsmodul, så Kunden kan håndtere rettighedsstyring via rammearkitekturen.\n\nIndtil Støttesystemerne er leveret, kan brugernes rettigheder udstilles direkte via Kundens AD FS som claims i det SAML 2.0 token som udstedes af denne.\n\nDet betyder også at Løsningen ikke må stille krav til administration af adgange og rettigheder inde i selve Løsningen.', '1');
INSERT INTO purchase_requirement (id, requirement_id, purchase_id, name, importance, description, category_id) VALUES ('3', '6', '1', 'Overholdelse af gældende lovgivning', 'HIGH', 'Løsningen skal til enhver tid overholde gældende lovgivning, herunder Persondataloven, Databeskyttelsesforordningen, It-sikkerhedsbekendtgørelsen samt gældende lovgivning indenfor sundhed- og omsorgsområdet.\n\nDette inkluderer også sletning af data i Løsningen, i henhold til lovgivningens krav om dette.', '4');
INSERT INTO purchase_requirement (id, requirement_id, purchase_id, name, importance, description, category_id) VALUES ('4', '5', '1', 'Arkivering hos Statens Arkiver', 'HIGH', 'Løsningen skal understøtte bekendtgørelse nr. 1007 af 20. august 2010 fra Statens Arkiver [ARKIV-BEKENDT].\n\nAflevering til offentligt arkiv vil være omfattet af kundebetaling. Angiv i kommentarfeltet hvad betalingen maksimalt kan udgøre i kr. pr. arkiveringsversion.\n\nI forbindelse med fejl på udtræk laver Leverandøren, efter normal praksis, efterfølgende rettelse og nyt udtræk uden beregning.\n\nLeverandøren dækker udgifter i forbindelse med test af arkiveringsversioner der ikke kan godkendes af Syddjurs Kommunearkiv og dermed ikke overholder afleveringsbestemmelsen udstedt af Syddjurs Kommunearkiv samt bekendtgørelse nr. 1007 af 20. august 2010.\n\nSyddjurs Kommunearkiv har outsourcet visse arkivopgaver til Aalborg Stadsarkiv, hvorfor Leverandøren accepterer at Aalborg Stadsarkiv kan fungere som Syddjurs Kommunearkivs proxy.', '3');

UPDATE requirement SET last_changed = CURRENT_TIMESTAMP WHERE id > 0;