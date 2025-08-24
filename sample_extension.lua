-- @name Sample Novel Extension
-- @version 1.0.0
-- @language en
-- @description A sample Lua extension for demonstrating functionality

-- Helper function to make HTTP requests (would be provided by the runtime)
function httpGet(url)
    -- This would be implemented by the native runtime
    return ""
end

-- Helper function to parse HTML (would be provided by the runtime)
function parseHtml(html)
    -- This would be implemented by the native runtime using JSoup
    return {}
end

-- Search for novels
function search(query)
    local results = {}
    
    -- Sample search results
    local sampleResults = {
        {
            id = "novel1",
            title = "Sample Novel " .. query,
            author = "Sample Author",
            description = "A sample novel that matches your search for " .. query,
            imageUrl = "https://example.com/cover1.jpg",
            url = "https://example.com/novel1"
        },
        {
            id = "novel2", 
            title = "Another Novel " .. query,
            author = "Another Author",
            description = "Another sample novel for " .. query,
            imageUrl = "https://example.com/cover2.jpg",
            url = "https://example.com/novel2"
        }
    }
    
    return sampleResults
end

-- Get detailed information about a novel
function getBook(id)
    local book = {
        id = id,
        title = "Sample Novel " .. id,
        author = "Sample Author",
        description = "This is a detailed description of the novel with ID " .. id .. ". It contains multiple chapters and is a great read.",
        imageUrl = "https://example.com/cover_" .. id .. ".jpg",
        url = "https://example.com/novel/" .. id,
        genres = {"Fantasy", "Adventure", "Romance"},
        status = "Ongoing",
        lastUpdated = 1640995200000 -- Unix timestamp
    }
    
    return book
end

-- Get a specific chapter
function getChapter(bookId, chapterId)
    local chapter = {
        id = chapterId,
        title = "Chapter " .. chapterId .. ": The Beginning",
        content = [[
            <h1>Chapter ]] .. chapterId .. [[: The Beginning</h1>
            <p>This is the content of chapter ]] .. chapterId .. [[ from book ]] .. bookId .. [[.</p>
            <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</p>
            <p>Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>
            <p>Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.</p>
        ]],
        url = "https://example.com/novel/" .. bookId .. "/chapter/" .. chapterId,
        order = tonumber(chapterId) or 1,
        releaseDate = 1640995200000
    }
    
    return chapter
end

-- Get the list of chapters for a novel
function getChapters(bookId)
    local chapters = {}
    
    -- Generate 10 sample chapters
    for i = 1, 10 do
        local chapter = {
            id = tostring(i),
            title = "Chapter " .. i .. ": " .. generateChapterTitle(i),
            content = "", -- Content would be loaded separately
            url = "https://example.com/novel/" .. bookId .. "/chapter/" .. i,
            order = i,
            releaseDate = 1640995200000 + (i * 86400000) -- One day apart
        }
        table.insert(chapters, chapter)
    end
    
    return chapters
end

-- Helper function to generate chapter titles
function generateChapterTitle(chapterNum)
    local titles = {
        "The Beginning",
        "First Steps", 
        "New Discoveries",
        "Unexpected Encounters",
        "Rising Challenges",
        "The Plot Thickens",
        "Turning Point",
        "Climactic Moments",
        "Resolution",
        "New Horizons"
    }
    
    return titles[chapterNum] or "Chapter " .. chapterNum
end
