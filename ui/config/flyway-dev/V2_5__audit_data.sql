-- get current timestamp converted to long
SET @revtimestamp = ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000);
INSERT INTO revinfo(rev,revtstmp) VALUES (1,@revtimestamp);
INSERT INTO
	requirement_aud(rev,revtype,id,name,cvr,request_share,available_for_all_domains,available_for_all_tags,importance,category_id,description,notes,rationale,info_requirement,last_changed,help_text,interested_party,favorite,relevant_for_onpremise,relevant_for_saas,request_share_email)
	SELECT 1,0,id,name,cvr,request_share,available_for_all_domains,available_for_all_tags,importance,category_id,description,notes,rationale,info_requirement,last_changed,help_text,interested_party,favorite,relevant_for_onpremise,relevant_for_saas,request_share_email
	FROM requirement;

INSERT INTO category_aud(rev,revtype,id,name) SELECT 1,0,id,name FROM category;

INSERT INTO domain_aud(rev,revtype,id,name) SELECT 1,0,id,name FROM domain;

INSERT INTO requirement_domain_aud(rev,revtype,requirement_id,domain_id) SELECT 1,0,requirement_id,domain_id FROM requirement_domain;

INSERT INTO tag_aud(rev,revtype,id,name,question) SELECT 1,0,id,name,question FROM tag;

INSERT INTO requirement_tag_aud(rev,revtype,requirement_id,tag_id) SELECT 1,0,requirement_id,tag_id FROM requirement_tag;

INSERT INTO architecture_principle_aud(rev,revtype,id,name,reference) SELECT 1,0,id,name,reference FROM architecture_principle;

INSERT INTO requirement_principle_aud(rev,revtype,requirement_id,architecture_principle_id) SELECT 1,0,requirement_id,architecture_principle_id FROM requirement_principle;

INSERT INTO attachment_aud(rev,revtype,id,name,url,requirement_id) SELECT 1,0,id,name,url,requirement_id FROM attachment;



