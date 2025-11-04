CREATE TABLE reviews(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_id BIGINT NOT NULL,
    created_by TEXT NOT NULL,
    comment TEXT,
    rate INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CHECK (rate BETWEEN 1 AND 5)
);

CREATE UNIQUE INDEX menu_id_created_by_idx ON reviews(menu_id, created_by);

CREATE TABLE ratings(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_id BIGINT NOT NULL UNIQUE,
    rate_one INTEGER NOT NULL DEFAULT 0,
    rate_two INTEGER NOT NULL DEFAULT 0,
    rate_three INTEGER NOT NULL DEFAULT 0,
    rate_four INTEGER NOT NULL DEFAULT 0,
    rate_five INTEGER NOT NULL DEFAULT 0,
    wilson_score FLOAT NOT NULL DEFAULT 0.0,
    avg_stars FLOAT NOT NULL DEFAULT 0.0
);

CREATE OR REPLACE FUNCTION calculate_wilson_score()
RETURNS TRIGGER AS $$
DECLARE
       positive FLOAT;
       negative FLOAT;
       prevWilsonScore FLOAT;
       wilsonScore FLOAT;
       total INTEGER;
BEGIN
    SELECT (rate_five * 1.0) + (rate_four * 0.75) + (rate_three * 0.5) + (rate_two * 0.25) + (rate_one * 0.0),
           (rate_five * 0.0) + (rate_four * 0.25) + (rate_three * 0.5) + (rate_two * 0.75) + (rate_one * 1.0),
           rate_one + rate_two + rate_three + rate_four + rate_five,
           wilson_score
    INTO positive, negative, total, prevWilsonScore
    FROM ratings
    WHERE menu_id = NEW.menu_id;

    IF positive + negative > 0 THEN
        wilsonScore := (
                            (positive + 1.9208) / (positive + negative) -
                            1.96 * SQRT((positive * negative) / (positive + negative) + 0.9604) / (positive + negative)
                        ) / (1 + 3.8416 / (positive + negative));

        IF prevWilsonScore != wilsonScore THEN
            UPDATE ratings
            SET wilson_score = wilsonScore,
                avg_stars = ROUND(CAST((((positive / total) * 4) + 1) * 2 AS NUMERIC), 2) / 2
            WHERE menu_id = NEW.menu_id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_wilson_score
AFTER INSERT OR UPDATE on ratings
FOR EACH ROW EXECUTE FUNCTION calculate_wilson_score();