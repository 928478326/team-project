-- Register gracie and henry in the SQLite app before running this fixture.
.bail on
PRAGMA foreign_keys = ON;
BEGIN;

INSERT INTO communities (community_id, name) VALUES ('study', 'Study')
ON CONFLICT(community_id) DO NOTHING;

INSERT INTO events (event_id, community_id, creator_id, title, description, capacity, starts_at_ms)
VALUES ('rating-demo-robarts', 'study', (SELECT user_id FROM users WHERE username = 'gracie'),
        'Robarts study meetup', 'Past meetup for the buddy ratings demo.', 2, 1735732800000)
ON CONFLICT(event_id) DO NOTHING;

INSERT INTO participations (user_id, event_id, joined_at_ms) VALUES
    ((SELECT user_id FROM users WHERE username = 'gracie'), 'rating-demo-robarts', 1735646400000),
    ((SELECT user_id FROM users WHERE username = 'henry'), 'rating-demo-robarts', 1735646400000)
ON CONFLICT(user_id, event_id) DO NOTHING;

COMMIT;
