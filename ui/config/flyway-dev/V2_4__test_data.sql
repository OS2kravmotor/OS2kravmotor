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

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Integration med Outlook", "36074051", "HIGH", "Løsningen skal kunne integreres med Microsoft Outlook, således at sagsbehandleren kan journalisere mails.", 0, 7);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Integration med Støttesystemener", "36074051", "HIGH", "Løsningen skal integreres med de kommende Støttesystemer, herunder skal løsningen levere data ind til Støttesystemet ’Sags- og Dokumentindekset’ [SAGDOK]

Endvidere skal Løsningen integrere med Støttesystemet ’Beskedfordeler’ [BESKED], hvor Løsningen skal publicere notifikationsbeskeder når der sker hændelser i Løsningen som har relevans for Kundens andre it-systemer.

Leverandøren skal dokumentere de notifikationsbeskeder som Løsningen udsender.", 0, 7);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Desktop understøttelse", "00000000", "HIGH", "I det omfang Løsningen helt eller delvist består af en rig klient (desktop), skal følgende platforme være understøttet

Windows 7 og Windows 10

Løsningen må basere sig på gængs Windows teknologi, herunder .Net frameworket, men må ikke kræve installation af yderligere 3.parts frameworks herunder Java, med mindre disse er helt indkapslet i den leverede Løsning.

Leverandøren må ikke ændre på krav til platforme i kontraktperioden uden skriftlig tillades fra Kunden.", 0, 8);

INSERT INTO requirement (name, cvr, importance, description, request_share, category_id) VALUES(
    "Mobil understøttelse", "00000000", "HIGH", "Løsningen skal tilbyde adgang via smartdevices, herunder mobil og tabletenheder. Følgende platforme skal være understøttet

Android version 4.4 og nyere
iOS version 10 og nyere

I det omfang Løsningen baserer sig på anden teknologi end installerbare apps (fx mobil-optimerede browser løsninger), skal dette fremgå af tilbuddet.

Leverandøren må ikke ændre på krav til platforme i kontraktperioden uden skriftlig tillades fra Kunden.", 0, 8);


INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(1, 1);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(2, 1);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(3, 2);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(4, 2);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(5, 3);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(6, 3);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(7, 4);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(8, 4);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(9, 5);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(10, 5);
INSERT INTO requirement_domain (requirement_id, domain_id) VALUES(11, 6);

INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(1, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(2, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(3, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(4, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(5, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(6, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(7, 3);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(8, 3);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(9, 1);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(10, 2);
INSERT INTO requirement_tag (requirement_id, tag_id) VALUES(11, 3);

INSERT INTO global_editor (user_id, cvr) VALUES ('user1', '36074051');

INSERT INTO identity_provider (name, cvr, entity_id, metadata) VALUES ('Test AD FS v1', '36074051', 'http://demo-adfs.digital-identity.dk/adfs/services/trust', 'https://demo-adfs.digital-identity.dk/federationmetadata/2007-06/federationmetadata.xml');

INSERT INTO community (id, name, community_cvr) VALUES ('1', 'Community 1', '11111111');
INSERT INTO community (id, name, community_cvr) VALUES ('2', 'Community 2', '22222222');

INSERT INTO community_member (id, municipality_cvr, community_id) VALUES ('1', '36074051', '1');
INSERT INTO community_member (id, municipality_cvr, community_id) VALUES ('2', '36074051', '2');

INSERT INTO it_system (id,system_id,name,vendor) VALUES (1, 3320, 'One', 'Digital-Identity');
INSERT INTO it_system (id,system_id,name,vendor) VALUES (2, 3321, 'Two', 'Digital-Identity');
INSERT INTO it_system (id,system_id,name,vendor) VALUES (3, 3322, 'Vinlo', 'Google');

INSERT INTO architecture_principle (name,reference) VALUES ('p1', 'princip 1');
INSERT INTO architecture_principle (name,reference) VALUES ('p2', 'princip 2');
INSERT INTO architecture_principle (name,reference) VALUES ('p3', 'princip 3');

UPDATE requirement SET last_changed = CURRENT_TIMESTAMP WHERE id > 0;
